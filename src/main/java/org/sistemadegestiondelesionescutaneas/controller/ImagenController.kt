package org.sistemadegestiondelesionescutaneas.controller

import org.sistemadegestiondelesionescutaneas.exception.StorageFileNotFoundException
import org.sistemadegestiondelesionescutaneas.model.ImagenLesion // Añadido
import org.sistemadegestiondelesionescutaneas.model.Paciente // Añadido
import org.sistemadegestiondelesionescutaneas.model.Usuario
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio // Añadido
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio
import org.sistemadegestiondelesionescutaneas.service.ImagenStorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource // Añadido para serveFile
import org.springframework.core.io.UrlResource // Añadido para serveFile
import org.springframework.http.HttpHeaders // Añadido para serveFile
import org.springframework.http.MediaType // Añadido para serveFile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model // Añadido
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.IOException
import java.net.MalformedURLException // Añadido para serveFile
import java.nio.file.Files // Añadido para serveFile
import java.nio.file.Path // Añadido para serveFile
// import java.util.List; // Comentado ya que List es de kotlin.collections en este contexto

@Controller
@RequestMapping("/imagenes")
class ImagenController {

    @Autowired
    private lateinit var imagenStorageService: ImagenStorageService

    @Autowired
    private lateinit var usuarioRepositorio: Usuariorepositorio

    @Autowired
    private lateinit var pacienterepositorio: Pacienterepositorio // Inyecta el repositorio de Paciente

    @Autowired // Añadido este repositorio
    private lateinit var imagenLesionRepositorio: ImagenLesionrepositorio

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
        val usuario: Usuario = usuarioRepositorio.findByUsuario(username)
            ?: run { // Reemplazado !! con operador elvis y bloque run
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.")
                return "redirect:/login" // O una página de error
            }

        // Asumiendo que un Usuario con rol PACIENTE tiene un Paciente asociado
        // y la relación está configurada en el modelo Usuario <-> Paciente.
        // Necesitas una forma de obtener el Paciente desde el Usuario.
        // Si la relación Usuario -> Paciente (OneToOne) se llama 'perfilPaciente' en Usuario:
        var paciente: Paciente? = usuario.perfilPaciente // Ahora es anulable explícitamente
        if (paciente == null) {
            // Intenta encontrar al paciente por el ID del usuario si perfilPaciente no está directamente poblado
            // Esto asume que Paciente tiene un enlace directo o un esquema de ID compartido.
            // El comentario original sobre cómo obtener Paciente era complejo.
            // Por ahora, asumiremos que usuario.perfilPaciente es la forma principal. Si es nulo para un rol PACIENTE,
            // usualmente indica un problema durante el registro o inconsistencia de datos.

            // Si el usuario es un PACIENTE, debería tener un perfilPaciente.
            // Si esto es nulo, es probablemente un problema.
            // El código original tenía: paciente = pacienterepositorio!!.findById(usuario.id).orElse(null)
            // Esto implica que el ID de Paciente podría ser el mismo que el ID de Usuario. Esto necesita ser confirmado desde el diseño del esquema de BD.
            // Por ahora, asumiremos que 'usuario.perfilPaciente' debería estar establecido.
            // Si no, es una condición de error para un PACIENTE subiendo para sí mismo.
            if ("PACIENTE".equals(usuario.rol, ignoreCase = true)) { // Comprueba el rol del usuario
                logger.error("No se encontró el perfil de paciente para el usuario PACIENTE: $username")
                redirectAttributes.addFlashAttribute("errorMessage", "Perfil de paciente no encontrado. Contacte a soporte.")
                return "redirect:/dashboard-paciente" // O página de error/dashboard apropiada
            } else {
                // Si un MEDICO o ADMIN está subiendo, necesitarían especificar para *cuál* paciente.
                // Esta lógica no está presente, así que asumiremos por ahora que solo los pacientes suben para sí mismos.
                logger.error("Lógica de carga para rol ${usuario.rol} no implementada o paciente no especificado.")
                redirectAttributes.addFlashAttribute("errorMessage", "Funcionalidad no disponible para su rol o paciente no especificado.")
                return "redirect:/"
            }
        }


        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor seleccione un archivo para cargar.")
            return "redirect:/dashboard-paciente" // O la página desde donde se sube
        }

        try {
            // El 'paciente' aquí debe ser no nulo para el método store.
            // El bloque anterior debería asegurar que 'paciente' se encuentre o redirigir.
            imagenStorageService.store(file, paciente!!) // Añadido !! asumiendo que 'paciente' está garantizado como no nulo aquí
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

    @GetMapping("/historial")
    fun mostrarHistorial( // Sintaxis corregida
        model: Model, // Sintaxis corregida
        authentication: Authentication?,
        redirectAttributes: RedirectAttributes
    ): String { // Sintaxis corregida
        if (authentication == null || !authentication.isAuthenticated) { // 'isAuthenticated' es una propiedad
            return "redirect:/login";
        }
        val username = authentication.name; // Sintaxis corregida
        val usuario: Usuario = usuarioRepositorio.findByUsuario(username) // Sintaxis corregida y manejo de nulos
            ?: run {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
                return "redirect:/login";
            }

        val paciente: Paciente = usuario.perfilPaciente // Sintaxis corregida y manejo de nulos, getPerfilPaciente() se accede como propiedad
            ?: run {
// Podrías intentar buscarlo de otra forma si la relación directa no está poblada
                logger.warn("No se encontró el perfil de paciente para el usuario: " + username + " al ver historial.");
// Maneja este caso, quizás redirigiendo con un error o a una página para crear perfil.
// Por ahora, asumimos que si es paciente, el perfil existe.
                redirectAttributes.addFlashAttribute("errorMessage", "Perfil de paciente no encontrado para ver historial.");
                return "redirect:/dashboard-paciente"; // O "/"
            }

        // Corregido: Usando el imagenLesionRepositorio autowired
        val imagenes: List<ImagenLesion> = imagenLesionRepositorio.findByPacienteOrderByFechaSubidaDesc(paciente);
        model.addAttribute("imagenes", imagenes);
        return "historial-imagenes"; // Nueva plantilla HTML
    }

    @GetMapping("/view/{filename:.+}")
    fun serveFile(@PathVariable filename: String): ResponseEntity<Resource> { // Convertido a Kotlin
        try {
            val file: Path = imagenStorageService.load(filename) // Usa el método load del servicio
            val resource: Resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                var contentType: String? = null
                try {
                    contentType = Files.probeContentType(file)
                } catch (e: IOException) {
                    logger.warn("No se pudo determinar el tipo de contenido para el archivo: $filename", e)
                }
                if(contentType == null) {
                    // Intenta adivinar basado en la extensión si probeContentType falla
                    contentType = when {
                        filename.lowercase().endsWith(".png") -> MediaType.IMAGE_PNG_VALUE
                        filename.lowercase().endsWith(".jpg") || filename.lowercase().endsWith(".jpeg") -> MediaType.IMAGE_JPEG_VALUE
                        else -> MediaType.APPLICATION_OCTET_STREAM_VALUE
                    }
                }

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"") // Uso de string template de Kotlin
                    .body(resource);
            } else {
                logger.error("No se pudo leer el archivo: $filename");
// Podrías devolver una imagen placeholder o un 404 específico
                return ResponseEntity.notFound().build();
            }
        } catch (e: MalformedURLException) {
            logger.error("Error al formar la URL para el archivo: $filename", e);
            return ResponseEntity.badRequest().build();
        } catch (e: StorageFileNotFoundException) { // Si load puede lanzar esto por no encontrado
            logger.error("Archivo no encontrado al servir: $filename", e)
            return ResponseEntity.notFound().build()
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
        val currentUser: Usuario = usuarioRepositorio.findByUsuario(username)
            ?: run {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.")
                return "redirect:/login"
            }

        try {
            imagenStorageService.delete(imagenId, currentUser)
            redirectAttributes.addFlashAttribute("successMessage", "Imagen eliminada correctamente.")
        } catch (e: StorageFileNotFoundException) {
            logger.warn("Intento de eliminar imagen no encontrada ID: $imagenId", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error: La imagen no existe.")
        } catch (e: AccessDeniedException) {
            logger.warn("Acceso denegado al intentar eliminar imagen ID: $imagenId por usuario: $username", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error: No tienes permiso para eliminar esta imagen.")
        } catch (e: IOException) {
            logger.error("Error de E/S al eliminar imagen ID: $imagenId", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la imagen: ${e.message}")
        }
        return "redirect:/imagenes/historial"
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ImagenController::class.java)
    }
}