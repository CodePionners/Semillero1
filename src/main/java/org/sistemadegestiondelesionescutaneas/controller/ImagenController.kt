package org.sistemadegestiondelesionescutaneas.controller

import org.sistemadegestiondelesionescutaneas.exception.StorageFileNotFoundException
import org.sistemadegestiondelesionescutaneas.model.ImagenLesion
import org.sistemadegestiondelesionescutaneas.model.Paciente
import org.sistemadegestiondelesionescutaneas.model.Usuario
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio
// import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio // No parece usarse directamente aquí
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio
import org.sistemadegestiondelesionescutaneas.service.ImagenStorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path

@Controller
@RequestMapping("/imagenes")
class ImagenController {

    @Autowired
    private lateinit var imagenStorageService: ImagenStorageService

    @Autowired
    private lateinit var usuarioRepositorio: Usuariorepositorio

    // Pacienterepositorio no se usa directamente, se accede a Paciente a través de Usuario.perfilPaciente
    // @Autowired
    // private lateinit var pacienterepositorio: Pacienterepositorio

    @Autowired
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
            ?: run {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.")
                return "redirect:/login"
            }

        val paciente: Paciente = usuario.perfilPaciente // Asumimos que si el rol es PACIENTE, perfilPaciente existe.
            ?: run {
                if ("PACIENTE".equals(usuario.rol, ignoreCase = true)) {
                    logger.error("No se encontró el perfil de paciente para el usuario PACIENTE: $username")
                    redirectAttributes.addFlashAttribute("errorUploadMessage", "Perfil de paciente no encontrado. Contacte a soporte.")
                } else {
                    logger.error("Lógica de carga para rol ${usuario.rol} no implementada o paciente no especificado.")
                    redirectAttributes.addFlashAttribute("errorUploadMessage", "Funcionalidad no disponible para su rol o paciente no especificado.")
                    // Si no es paciente y no tiene perfil, redirigir a la raíz o dashboard de su rol si existe
                    return "redirect:/"
                }
                return "redirect:/imagenes/historial" // MODIFICADO: Si es paciente y no tiene perfil, va a su dashboard (historial)
            }

        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("errorUploadMessage", "Por favor seleccione un archivo para cargar.")
            return "redirect:/imagenes/historial" // MODIFICADO
        }

        try {
            imagenStorageService.store(file, paciente)
            redirectAttributes.addFlashAttribute(
                "successUploadMessage",
                "Archivo cargado exitosamente: " + file.originalFilename
            )
        } catch (e: IOException) {
            logger.error("Error de E/S al cargar el archivo: " + file.originalFilename, e)
            redirectAttributes.addFlashAttribute(
                "errorUploadMessage",
                "Error de E/S al cargar el archivo: " + e.message
            )
        } catch (e: Exception) {
            logger.error("Error inesperado al cargar el archivo: " + file.originalFilename, e)
            redirectAttributes.addFlashAttribute(
                "errorUploadMessage",
                "Error inesperado al cargar el archivo: " + e.message
            )
        }
        return "redirect:/imagenes/historial" // MODIFICADO
    }

    @GetMapping("/historial")
    fun mostrarHistorial(
        model: Model,
        authentication: Authentication?,
        redirectAttributes: RedirectAttributes // Para posibles errores al cargar la página
    ): String {
        if (authentication == null || !authentication.isAuthenticated) {
            // No añadir flash attribute aquí ya que es una redirección GET simple
            return "redirect:/login"
        }
        val username = authentication.name
        val usuario: Usuario = usuarioRepositorio.findByUsuario(username)
            ?: run {
                // Considera loguear esto y redirigir a login sin mensaje,
                // o con un mensaje genérico si se usa model.addAttribute ANTES de redirigir
                redirectAttributes.addFlashAttribute("errorMessage", "Sesión inválida o usuario no encontrado.")
                return "redirect:/login"
            }

        // Solo los pacientes deben acceder directamente a esta vista a través de este método.
        // Otros roles podrían tener sus propias vistas de historial o acceder a través de otros medios.
        if (!"PACIENTE".equals(usuario.rol, ignoreCase = true)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Acceso no autorizado para este rol.")
            return "redirect:/" // Redirige a la página principal para otros roles
        }

        val paciente: Paciente? = usuario.perfilPaciente
        if (paciente == null) {
            logger.warn("No se encontró el perfil de paciente para el usuario: $username al ver historial.")
            // Añadir mensaje al modelo para ser mostrado en historial-imagenes.html en vez de RedirectAttributes
            model.addAttribute("errorMessage", "Perfil de paciente no encontrado. Contacte a soporte.")
            model.addAttribute("imagenes", emptyList<ImagenLesion>()) // Enviar lista vacía
            return "historial-imagenes" // Mostrar la página con el error
        }

        try {
            val imagenes: List<ImagenLesion> = imagenLesionRepositorio.findByPacienteOrderByFechaSubidaDesc(paciente)
            model.addAttribute("imagenes", imagenes)
        } catch (e: Exception) {
            logger.error("Error al cargar el historial de imágenes para el paciente ${paciente.id}", e)
            model.addAttribute("errorMessage", "Error al cargar el historial de imágenes.")
            model.addAttribute("imagenes", emptyList<ImagenLesion>())
        }
        return "historial-imagenes"
    }

    @GetMapping("/view/{filename:.+}")
    @ResponseBody
    fun serveFile(@PathVariable filename: String): ResponseEntity<Resource> {
        try {
            val file: Path = imagenStorageService.load(filename)
            val resource: Resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                var contentType: String? = null
                try {
                    contentType = Files.probeContentType(file)
                } catch (e: IOException) {
                    logger.warn("No se pudo determinar el tipo de contenido para el archivo: $filename", e)
                }
                if (contentType == null) {
                    contentType = when {
                        filename.lowercase().endsWith(".png") -> MediaType.IMAGE_PNG_VALUE
                        filename.lowercase().endsWith(".jpg") || filename.lowercase().endsWith(".jpeg") -> MediaType.IMAGE_JPEG_VALUE
                        else -> MediaType.APPLICATION_OCTET_STREAM_VALUE
                    }
                }

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    // .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"") // Para visualización en línea
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${resource.filename}\"") // Para forzar descarga
                    .body(resource)
            } else {
                logger.error("No se pudo leer el archivo o no existe: $filename")
                throw StorageFileNotFoundException("No se pudo leer el archivo: $filename")
            }
        } catch (e: MalformedURLException) {
            logger.error("Error al formar la URL para el archivo: $filename", e)
            // No es un error del cliente realmente, sino del servidor o configuración
            return ResponseEntity.internalServerError().build()
        } catch (e: StorageFileNotFoundException) {
            logger.warn("Archivo no encontrado al servir: $filename", e)
            return ResponseEntity.notFound().build()
        } catch (e: IOException) { // Otros errores de I/O al intentar acceder al recurso
            logger.error("Error de I/O al servir el archivo $filename", e)
            return ResponseEntity.internalServerError().build()
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
            redirectAttributes.addFlashAttribute("errorMessage", "Error: La imagen no existe o ya fue eliminada.")
        } catch (e: AccessDeniedException) {
            logger.warn("Acceso denegado al intentar eliminar imagen ID: $imagenId por usuario: $username", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error: No tienes permiso para eliminar esta imagen.")
        } catch (e: IOException) {
            logger.error("Error de E/S al eliminar imagen ID: $imagenId", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la imagen: ${e.message}")
        } catch (e: Exception) {
            logger.error("Error inesperado al eliminar imagen ID: $imagenId", e)
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al eliminar la imagen.")
        }
        return "redirect:/imagenes/historial"
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ImagenController::class.java)
    }
}