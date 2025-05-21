package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.*;
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio;
import org.sistemadegestiondelesionescutaneas.repository.AnalisisDermatologicorepositorio; // Para Fase 5
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils; // Para limpiar nombres de archivo
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sistemadegestiondelesionescutaneas.exception.StorageFileNotFoundException;
import org.springframework.security.access.AccessDeniedException;





@Service
public class ImagenStorageService {

    private static final Logger logger = LoggerFactory.getLogger(ImagenStorageService.class);
    private final Path rootLocation;
    private final ImagenLesionrepositorio imagenLesionRepositorio;
    private final AnalisisDermatologicorepositorio analisisDermatologicoRepositorio; // Para Fase 5

    @Autowired
    public ImagenStorageService(@Value("${app.upload.dir}") String uploadDir,
                                ImagenLesionrepositorio imagenLesionRepositorio,
                                AnalisisDermatologicorepositorio analisisDermatologicoRepositorio) { // Para Fase 5
        this.rootLocation = Paths.get(uploadDir);
        this.imagenLesionRepositorio = imagenLesionRepositorio;
        this.analisisDermatologicoRepositorio = analisisDermatologicoRepositorio; // Para Fase 5
        try {
            Files.createDirectories(rootLocation);
            logger.info("Directorio de carga creado/verificado en: " + rootLocation.toAbsolutePath());
        } catch (IOException e) {
            logger.error("No se pudo inicializar el directorio de almacenamiento de imágenes", e);
            throw new RuntimeException("No se pudo inicializar el directorio de almacenamiento", e);
        }
    }

    public void store(MultipartFile file, Paciente paciente) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        if (file.isEmpty()) {
            throw new IOException("Fallo al almacenar archivo vacío " + uniqueFilename);
        }
        if (uniqueFilename.contains("..")) {
            // Esto es una comprobación de seguridad
            throw new IOException(
                    "No se puede almacenar archivo con ruta relativa fuera del directorio actual "
                            + uniqueFilename);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // Esto es una comprobación de seguridad adicional
                throw new IOException(
                        "No se puede almacenar archivo fuera del directorio actual.");
            }
            Files.copy(inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING);
            logger.info("Archivo guardado físicamente en: " + destinationFile);


            ImagenLesion imagenLesion = new ImagenLesion();
            imagenLesion.setRutaArchivo(uniqueFilename); // Almacena solo el nombre del archivo único
            imagenLesion.setFechaSubida(LocalDateTime.now());
            imagenLesion.setPaciente(paciente);
            ImagenLesion savedImage = imagenLesionRepositorio.save(imagenLesion);
            logger.info("Registro de ImagenLesion guardado con ID: " + savedImage.getId());

            // ----- INICIO FASE 5.1 (Opción A: Análisis básico automático) -----
            AnalisisDermatologico analisis = new AnalisisDermatologico();
            analisis.setPaciente(paciente);
            analisis.setImagen(savedImage); // Vincula con la imagen guardada
            analisis.setFechahoraanalisis(LocalDateTime.now());
            analisis.setDiagnostico(Diagnostico.INDETERMINADO); // O PENDIENTE
            // Establece otros campos obligatorios de AnalisisDermatologico como placeholders o desde Paciente
            if (paciente.getSexo() != null) analisis.setSexo(paciente.getSexo()); else analisis.setSexo(Sexo.OTRO); // Ejemplo
            analisis.setEdadestimada(EdadEstimada.DESCONOCIDA); // Ejemplo
            analisis.setAreacorporalafectada(AreaCorporalAfectada.NO_ESPECIFICADA); // Ejemplo
            analisis.setTipopielfitzpatrick(TipoPielFitzpatrick.NO_ESPECIFICADO); // Ejemplo
            analisis.setTamanodelesion(TamanodeLesion.NO_MEDIDO); // Ejemplo
            analisis.setAntecedentesfamiliarescancer(AntecedentesFamiliaresCancer.NO_ESPECIFICADO); // Ejemplo
            analisis.setHistoriallesionesprevias(false); // Ejemplo
            analisisDermatologicoRepositorio.save(analisis);
            logger.info("Registro de AnalisisDermatologico (placeholder) creado para ImagenLesion ID: " + savedImage.getId());
            // ----- FIN FASE 5.1 -----


        } catch (IOException e) {
            logger.error("Fallo al almacenar archivo " + uniqueFilename, e);
            throw new IOException("Fallo al almacenar archivo " + uniqueFilename, e);
        }
    }
    // Método para cargar archivo (lo usarás en la Fase 3)
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public void delete(Long imagenId, Usuario currentUser) throws IOException {
        ImagenLesion imagen = imagenLesionRepositorio.findById(imagenId)
                .orElseThrow(() -> new StorageFileNotFoundException("No se encontró la imagen con ID: " + imagenId));

        // Autorización: Solo el paciente dueño o un médico/admin pueden borrar.
        // Esto es un ejemplo, ajusta la lógica de roles según sea necesario.
        boolean isOwner = imagen.getPaciente().getUsuario().getId().equals(currentUser.getId());
        boolean isAdminOrMedico = currentUser.getRol().equalsIgnoreCase("MEDICO") || currentUser.getRol().equalsIgnoreCase("ADMIN");

        if (!isOwner && !isAdminOrMedico) {
            logger.warn("Intento no autorizado de eliminar imagen ID: " + imagenId + " por usuario: " + currentUser.getUsuario());
            throw new AccessDeniedException("No tienes permiso para eliminar esta imagen.");
        }

        // Eliminar archivo físico
        Path filePath = load(imagen.getRutaArchivo());
        Files.deleteIfExists(filePath);
        logger.info("Archivo físico eliminado: " + filePath);


        // Antes de eliminar ImagenLesion, considera si necesitas eliminar AnalisisDermatologico asociado
        // si la relación tiene CascadeType.ALL u OrphanRemoval=true en ImagenLesion.
        // Tu ImagenLesion tiene @OneToOne(mappedBy = "imagen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
        // private AnalisisDermatologico analisisDermatologico;
        // Esto significa que al borrar ImagenLesion, el AnalisisDermatologico asociado se borrará automáticamente.

        // Eliminar registro de la base de datos
        imagenLesionRepositorio.delete(imagen);
        logger.info("Registro de ImagenLesion eliminado para ID: " + imagenId);

    }


}



