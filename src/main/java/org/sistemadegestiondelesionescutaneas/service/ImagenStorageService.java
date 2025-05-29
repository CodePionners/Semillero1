package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.*;
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio;
import org.sistemadegestiondelesionescutaneas.repository.AnalisisDermatologicorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
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
@Lazy
public class ImagenStorageService {

    private static final Logger logger = LoggerFactory.getLogger(ImagenStorageService.class);
    private final Path rootLocation;
    private final ImagenLesionrepositorio imagenLesionRepositorio;
    private final AnalisisDermatologicorepositorio analisisDermatologicoRepositorio;

    @Autowired
    public ImagenStorageService(@Value("${app.upload.dir}") String uploadDir,
                                ImagenLesionrepositorio imagenLesionRepositorio,
                                AnalisisDermatologicorepositorio analisisDermatologicoRepositorio) {
        this.imagenLesionRepositorio = imagenLesionRepositorio;
        this.analisisDermatologicoRepositorio = analisisDermatologicoRepositorio;
        // Resolver la ruta de subida. Es crucial entender que "." es relativo al directorio de trabajo actual.
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize(); // Normalizar y obtener ruta absoluta
        logger.info("Directorio raíz de almacenamiento configurado en: {}", this.rootLocation);

        try {
            Files.createDirectories(rootLocation); // Intenta crear el directorio si no existe
            logger.info("Directorio de carga creado/verificado en: {}", rootLocation);

            if (!Files.isWritable(rootLocation)) {
                logger.error("¡ALERTA! El directorio de carga {} NO tiene permisos de escritura.", rootLocation);
                // Considera lanzar una excepción aquí si no se puede escribir, para fallar rápido.
                // throw new RuntimeException("El directorio de carga no tiene permisos de escritura: " + rootLocation);
            } else {
                logger.info("El directorio de carga {} SÍ tiene permisos de escritura.", rootLocation);
            }

        } catch (IOException e) {
            logger.error("No se pudo inicializar el directorio de almacenamiento de imágenes en {}: {}", rootLocation, e.getMessage(), e);
            throw new RuntimeException("No se pudo inicializar el directorio de almacenamiento en " + rootLocation, e);
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
            logger.warn("Intento de almacenar archivo vacío: {}", uniqueFilename);
            throw new IOException("Fallo al almacenar archivo vacío " + uniqueFilename);
        }
        if (uniqueFilename.contains("..")) {
            logger.warn("Intento de almacenar archivo con ruta relativa peligrosa: {}", uniqueFilename);
            throw new IOException(
                    "No se puede almacenar archivo con ruta relativa fuera del directorio actual "
                            + uniqueFilename);
        }

        Path destinationFile = this.rootLocation.resolve(uniqueFilename)
                .normalize().toAbsolutePath(); // Normalizar para seguridad y consistencia

        logger.info("Intentando almacenar archivo. Original: '{}', Único: '{}', Destino Propuesto: '{}'",
                originalFilename, uniqueFilename, destinationFile);
        logger.info("Directorio raíz de almacenamiento actual: '{}'", this.rootLocation);


        if (!destinationFile.getParent().equals(this.rootLocation)) {
            logger.error("¡Fallo de seguridad! Intento de almacenar archivo fuera del directorio raíz. Destino: {}, Raíz: {}",
                    destinationFile.getParent(), this.rootLocation);
            throw new IOException(
                    "No se puede almacenar archivo fuera del directorio actual. Intento de Path Traversal.");
        }

        // Verificar explícitamente si el directorio es escribible justo antes de la copia
        if (!Files.isWritable(this.rootLocation)) {
            logger.error("El directorio de almacenamiento '{}' NO es escribible justo antes de la copia. Verifique los permisos.", this.rootLocation);
            throw new IOException("El directorio de almacenamiento no tiene permisos de escritura: " + this.rootLocation);
        }
        logger.debug("El directorio de almacenamiento '{}' parece ser escribible. Procediendo con la copia del archivo.", this.rootLocation);


        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING); // Aquí es donde ocurre la escritura física
            logger.info("Archivo guardado físicamente en: {}", destinationFile);

            ImagenLesion imagenLesion = new ImagenLesion();
            imagenLesion.setRutaArchivo(uniqueFilename);
            imagenLesion.setFechaSubida(LocalDateTime.now());
            imagenLesion.setPaciente(paciente);
            ImagenLesion savedImage = imagenLesionRepositorio.save(imagenLesion);
            logger.info("Registro de ImagenLesion guardado con ID: {}", savedImage.getId());

            AnalisisDermatologico analisis = new AnalisisDermatologico();
            analisis.setPaciente(paciente);
            analisis.setImagen(savedImage);
            analisis.setFechahoraanalisis(LocalDateTime.now());
            analisis.setDiagnostico(Diagnostico.INDETERMINADO);
            if (paciente.getSexo() != null) analisis.setSexo(paciente.getSexo()); else analisis.setSexo(Sexo.OTRO);
            analisis.setEdadestimada(EdadEstimada.DESCONOCIDA);
            analisis.setAreacorporalafectada(AreaCorporalAfectada.NO_ESPECIFICADA);
            analisis.setTipopielfitzpatrick(TipoPielFitzpatrick.NO_ESPECIFICADO);
            analisis.setTamanodelesion(TamanodeLesion.NO_MEDIDO);
            analisis.setAntecedentesfamiliarescancer(AntecedentesFamiliaresCancer.NO_ESPECIFICADO);
            analisis.setHistoriallesionesprevias(false);
            analisisDermatologicoRepositorio.save(analisis);
            logger.info("Registro de AnalisisDermatologico (placeholder) creado para ImagenLesion ID: {}", savedImage.getId());

        } catch (IOException e) {
            logger.error("Fallo CRÍTICO al almacenar/copiar el archivo '{}' en '{}'. Causa: {}", uniqueFilename, destinationFile, e.getMessage(), e);
            // La excepción original 'e' es la más importante aquí.
            throw new IOException("Fallo al almacenar archivo " + uniqueFilename, e); // Re-lanzar con el mensaje original pero envolviendo la causa
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public void delete(Long imagenId, Usuario currentUser) throws IOException {
        ImagenLesion imagen = imagenLesionRepositorio.findByIdWithPacienteAndUsuario(imagenId)
                .orElseThrow(() -> new StorageFileNotFoundException("No se encontró la imagen con ID: " + imagenId));

        boolean isOwner = imagen.getPaciente().getUsuario().getId().equals(currentUser.getId());
        boolean isAdminOrMedico = currentUser.getRol().equalsIgnoreCase("MEDICO") || currentUser.getRol().equalsIgnoreCase("ADMIN");

        if (!isOwner && !isAdminOrMedico) {
            logger.warn("Intento no autorizado de eliminar imagen ID: {} por usuario: {}", imagenId, currentUser.getUsuario());
            throw new AccessDeniedException("No tienes permiso para eliminar esta imagen.");
        }

        Path filePath = load(imagen.getRutaArchivo());
        Files.deleteIfExists(filePath);
        logger.info("Archivo físico eliminado: {}", filePath);

        imagenLesionRepositorio.delete(imagen);
        logger.info("Registro de ImagenLesion eliminado para ID: {}", imagenId);
    }
}