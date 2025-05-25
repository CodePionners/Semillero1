package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.*; //
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio; //
import org.sistemadegestiondelesionescutaneas.repository.AnalisisDermatologicorepositorio; //
import org.springframework.beans.factory.annotation.Autowired; //
import org.springframework.beans.factory.annotation.Value; //
import org.springframework.context.annotation.Lazy; //
import org.springframework.stereotype.Service; //
import org.springframework.web.multipart.MultipartFile; //
import org.springframework.util.StringUtils; //
import java.io.IOException; //
import java.io.InputStream; //
import java.nio.file.Files; //
import java.nio.file.Path; //
import java.nio.file.Paths; //
import java.nio.file.StandardCopyOption; //
import java.time.LocalDateTime; //
import java.util.UUID; //
import org.slf4j.Logger; //
import org.slf4j.LoggerFactory; //
import org.sistemadegestiondelesionescutaneas.exception.StorageFileNotFoundException; //
import org.springframework.security.access.AccessDeniedException; //
// import java.util.Optional; // No es necesario aquí si el repo lanza la excepción

@Service
@Lazy //
public class ImagenStorageService {

    private static final Logger logger = LoggerFactory.getLogger(ImagenStorageService.class); //
    private final Path rootLocation; //
    private final ImagenLesionrepositorio imagenLesionRepositorio; //
    private final AnalisisDermatologicorepositorio analisisDermatologicoRepositorio; //

    @Autowired
    public ImagenStorageService(@Value("${app.upload.dir}") String uploadDir, //
                                ImagenLesionrepositorio imagenLesionRepositorio, //
                                AnalisisDermatologicorepositorio analisisDermatologicoRepositorio) { //
        this.rootLocation = Paths.get(uploadDir); //
        this.imagenLesionRepositorio = imagenLesionRepositorio; //
        this.analisisDermatologicoRepositorio = analisisDermatologicoRepositorio; //
        try {
            Files.createDirectories(rootLocation); //
            logger.info("Directorio de carga creado/verificado en: " + rootLocation.toAbsolutePath()); //
        } catch (IOException e) { //
            logger.error("No se pudo inicializar el directorio de almacenamiento de imágenes", e); //
            throw new RuntimeException("No se pudo inicializar el directorio de almacenamiento", e); //
        }
    }

    public void store(MultipartFile file, Paciente paciente) throws IOException { //
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename()); //
        String extension = ""; //
        int i = originalFilename.lastIndexOf('.'); //
        if (i > 0) { //
            extension = originalFilename.substring(i); //
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension; //

        if (file.isEmpty()) { //
            throw new IOException("Fallo al almacenar archivo vacío " + uniqueFilename); //
        }
        if (uniqueFilename.contains("..")) { //
            // Esto es una verificación de seguridad, no directamente de rendimiento.
            throw new IOException( //
                    "No se puede almacenar archivo con ruta relativa fuera del directorio actual " //
                            + uniqueFilename); //
        }

        try (InputStream inputStream = file.getInputStream()) { //
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename)) //
                    .normalize().toAbsolutePath(); //
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) { //
                // Verificación de seguridad
                throw new IOException( //
                        "No se puede almacenar archivo fuera del directorio actual."); //
            }
            Files.copy(inputStream, destinationFile, //
                    StandardCopyOption.REPLACE_EXISTING); //
            logger.info("Archivo guardado físicamente en: " + destinationFile); //


            ImagenLesion imagenLesion = new ImagenLesion(); //
            imagenLesion.setRutaArchivo(uniqueFilename); //
            imagenLesion.setFechaSubida(LocalDateTime.now()); //
            imagenLesion.setPaciente(paciente); //
            ImagenLesion savedImage = imagenLesionRepositorio.save(imagenLesion); //
            logger.info("Registro de ImagenLesion guardado con ID: " + savedImage.getId()); //

            AnalisisDermatologico analisis = new AnalisisDermatologico(); //
            analisis.setPaciente(paciente); //
            analisis.setImagen(savedImage); //
            analisis.setFechahoraanalisis(LocalDateTime.now()); //
            analisis.setDiagnostico(Diagnostico.INDETERMINADO); //
            if (paciente.getSexo() != null) analisis.setSexo(paciente.getSexo()); else analisis.setSexo(Sexo.OTRO); //
            analisis.setEdadestimada(EdadEstimada.DESCONOCIDA); //
            analisis.setAreacorporalafectada(AreaCorporalAfectada.NO_ESPECIFICADA); //
            analisis.setTipopielfitzpatrick(TipoPielFitzpatrick.NO_ESPECIFICADO); //
            analisis.setTamanodelesion(TamanodeLesion.NO_MEDIDO); //
            analisis.setAntecedentesfamiliarescancer(AntecedentesFamiliaresCancer.NO_ESPECIFICADO); //
            analisis.setHistoriallesionesprevias(false); //
            analisisDermatologicoRepositorio.save(analisis); //
            logger.info("Registro de AnalisisDermatologico (placeholder) creado para ImagenLesion ID: " + savedImage.getId()); //

        } catch (IOException e) { //
            logger.error("Fallo al almacenar archivo " + uniqueFilename, e); //
            throw new IOException("Fallo al almacenar archivo " + uniqueFilename, e); //
        }
    }

    public Path load(String filename) { //
        return rootLocation.resolve(filename); //
    }

    public void delete(Long imagenId, Usuario currentUser) throws IOException { //
        // Utilizar el método optimizado del repositorio
        ImagenLesion imagen = imagenLesionRepositorio.findByIdWithPacienteAndUsuario(imagenId) //
                .orElseThrow(() -> new StorageFileNotFoundException("No se encontró la imagen con ID: " + imagenId)); //

        // El paciente y el usuario ya están cargados, no hay consultas adicionales aquí.
        boolean isOwner = imagen.getPaciente().getUsuario().getId().equals(currentUser.getId()); //
        boolean isAdminOrMedico = currentUser.getRol().equalsIgnoreCase("MEDICO") || currentUser.getRol().equalsIgnoreCase("ADMIN"); //

        if (!isOwner && !isAdminOrMedico) { //
            logger.warn("Intento no autorizado de eliminar imagen ID: " + imagenId + " por usuario: " + currentUser.getUsuario()); //
            throw new AccessDeniedException("No tienes permiso para eliminar esta imagen."); //
        }

        Path filePath = load(imagen.getRutaArchivo()); //
        Files.deleteIfExists(filePath); //
        logger.info("Archivo físico eliminado: " + filePath); //

        // La eliminación en cascada de AnalisisDermatologico debería manejarse
        // por la configuración de la relación en la entidad ImagenLesion si es @OneToOne(mappedBy = "imagen", cascade = CascadeType.ALL, ...)
        // Confirma que esto está configurado o elimina explícitamente el análisis si es necesario.
        // AnalisisDermatologico tiene una FK a ImagenLesion, así que la entidad ImagenLesion debe eliminarse
        // después (o JPA manejará el orden si la relación es bidireccional con cascade).
        // Si AnalisisDermatologico tiene una referencia directa a ImagenLesion, y ImagenLesion se elimina,
        // necesitarás eliminar AnalisisDermatologico primero si no hay cascade desde ImagenLesion.
        // La entidad ImagenLesion tiene: @OneToOne(mappedBy = "imagen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
        // private AnalisisDermatologico analisisDermatologico;
        // Esto significa que eliminar ImagenLesion SÍ debería eliminar el AnalisisDermatologico asociado.

        imagenLesionRepositorio.delete(imagen); //
        logger.info("Registro de ImagenLesion eliminado para ID: " + imagenId); //
    }
}