package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.exception.StorageFileNotFoundException
import org.sistemadegestiondelesionescutaneas.model.Usuario
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio
import org.sistemadegestiondelesionescutaneas.service.ImagenStorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.IOException
import java.util.List


@Controller
@RequestMapping("/imagenes")
class ImagenController {
    @Autowired
    private val imagenStorageService: ImagenStorageService? = null

    @Autowired
    private val usuarioRepositorio: Usuariorepositorio? = null

    @Autowired
    private val pacienterepositorio: Pacienterepositorio? = null // Inyecta el repositorio de Paciente

    @PostMapping("/upload")
    fun handleFileUpload(
        @RequestParam("file") file: MultipartFile,
        authentication: Authentication?,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (authentication == null || !authentication.isAuthenticated) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no autenticado.")
            return "redirect:/login"
        }

        val username = authentication.name
        val usuario = usuarioRepositorio!!.findByUsuario(username)
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.")
            return "redirect:/login" // O una página de error
        }

        // Asumiendo que un Usuario con rol PACIENTE tiene un Paciente asociado
        // y la relación está configurada en el modelo Usuario <-> Paciente.
        // Necesitas una forma de obtener el Paciente desde el Usuario.
        // Si la relación Usuario -> Paciente (OneToOne) se llama 'perfilPaciente' en Usuario:
        var paciente = usuario.perfilPaciente
        if (paciente == null) {
            // Si no se crea automáticamente o si es un médico subiendo para un paciente,
            // necesitarás una lógica diferente aquí (ej. seleccionar paciente).
            // Por ahora, asumimos que el paciente existe si el rol es PACIENTE.
            // Puede que necesites buscarlo explícitamente si no está directo en Usuario:
            paciente = pacienterepositorio!!.findById(usuario.id) // O si Paciente tiene un campo usuario_id:
                // paciente = pacienterepositorio.findByUsuarioId(usuario.getId())
                // Esto depende de tu modelo exacto y si id de usuario y paciente son el mismo.
                // La forma más robusta es si Paciente tiene una referencia directa a Usuario.
                // O si Usuario tiene una referencia directa a Paciente.
                // Tu modelo Usuario tiene: @OneToOne(mappedBy = "usuario") private Paciente perfilPaciente;
                // Así que `usuario.getPerfilPaciente()` debería funcionar si la relación se establece correctamente.
                .orElse(null) // O lanza una excepción/maneja error
            if (paciente == null) {
                logger.error("No se encontró el perfil de paciente para el usuario: $username")
                redirectAttributes.addFlashAttribute("errorMessage", "Perfil de paciente no encontrado.")
                return "redirect:/" // O a la página donde se sube la imagen
            }
        }


        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor seleccione un archivo para cargar.")
            return "redirect:/dashboard-paciente" // O la página desde donde se sube
        }

        try {
            imagenStorageService.store(file, paciente)
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Archivo cargado exitosamente: " + file.originalFilename
            )
        } catch (e: Exception) {
            logger.error("Error al cargar el archivo: " + file.originalFilename, e)
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Error al cargar el archivo: " + e.message
            )
        }

        return "redirect:/dashboard-paciente" // Redirige al dashboard del paciente
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ImagenController::class.java)
    }
    @GetMapping("/historial")
    public String mostrarHistorial(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByUsuario(username);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/login";
        }
        Paciente paciente = usuario.getPerfilPaciente();
        if (paciente == null) {
// Podrías intentar buscarlo de otra forma si la relación directa no está poblada
            logger.warn("No se encontró el perfil de paciente para el usuario: " + username + " al ver historial.");
// Maneja este caso, quizás redirigiendo con un error o a una página para crear perfil.
// Por ahora, asumimos que si es paciente, el perfil existe.
            redirectAttributes.addFlashAttribute("errorMessage", "Perfil de paciente no encontrado para ver historial.");
            return "redirect:/dashboard-paciente"; // O "/"
        }

        List<ImagenLesion> imagenes = imagenLesionRepositorio.findByPacienteOrderByFechaSubidaDesc(paciente);
        model.addAttribute("imagenes", imagenes);
        return "historial-imagenes"; // Nueva plantilla HTML
    }
    ```
    * Añade un método para servir los archivos de imagen:
    ```java
    import org.springframework.core.io.Resource;
    import org.springframework.core.io.UrlResource;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.PathVariable;
    import java.net.MalformedURLException;
    import java.nio.file.Path;
    // ... (otras importaciones)

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = imagenStorageService.load(filename); // Usa el método load del servicio
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                String contentType = null;
                try {
                    contentType = Files.probeContentType(file);
                } catch (IOException e) {
                    logger.warn("No se pudo determinar el tipo de contenido para el archivo: " + filename, e);
                }
                if(contentType == null) {
                    // Intenta adivinar basado en la extensión si probeContentType falla
                    if (filename.toLowerCase().endsWith(".png")) {
                        contentType = MediaType.IMAGE_PNG_VALUE;
                    } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                        contentType = MediaType.IMAGE_JPEG_VALUE;
                    } else {
                        contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                    }
                }

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="" + resource.getFilename() + """)
                    .body(resource);
            } else {
                logger.error("No se pudo leer el archivo: " + filename);
// Podrías devolver una imagen placeholder o un 404 específico
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            logger.error("Error al formar la URL para el archivo: " + filename, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/delete/{imagenId}")
    fun deleteImage(
        @PathVariable imagenId: Long,
        authentication: Authentication?,
        redirectAttributes: RedirectAttributes,
    ): String {
        if (authentication == null || !authentication.isAuthenticated) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no autenticado.")
            return "redirect:/login"
        }
        val username = authentication.name
        val currentUser = usuarioRepositorio!!.findByUsuario(username)
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.")
            return "redirect:/login"
        }

        try {
            imagenStorageService!!.delete(imagenId, currentUser)
            redirectAttributes.addFlashAttribute("successMessage", "Imagen eliminada correctamente.")
        } catch (e: StorageFileNotFoundException) {
            logger.warn("Intento de eliminar imagen no encontrada ID: $imagenId", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error: La imagen no existe.")
        } catch (e: AccessDeniedException) {
            logger.warn("Acceso denegado al intentar eliminar imagen ID: $imagenId por usuario: $username", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error: No tienes permiso para eliminar esta imagen.")
        } catch (e: IOException) {
            logger.error("Error de E/S al eliminar imagen ID: $imagenId", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la imagen: " + e.message)
        }
        return "redirect:/imagenes/historial"
    }



}