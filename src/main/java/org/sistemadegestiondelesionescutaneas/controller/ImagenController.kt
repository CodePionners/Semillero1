package org.sistemadegestiondelesionescutaneas.controller

import org.sistemadegestiondelesionescutaneas.exception.StorageFileNotFoundException
import org.sistemadegestiondelesionescutaneas.model.ImagenLesion
import org.sistemadegestiondelesionescutaneas.model.Paciente
import org.sistemadegestiondelesionescutaneas.model.Usuario
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio
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
import org.springframework.security.access.AccessDeniedException // Asegúrate que esta importación existe si la usas
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

    @Autowired
    private lateinit var pacienterepositorio: Pacienterepositorio

    @Autowired
    private lateinit var imagenLesionRepositorio: ImagenLesionrepositorio

    // IMPORTANTE: Asegúrate de que no haya ninguna otra declaración de clase aquí,
    // especialmente la de "Controladorautenticacion".

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

        var paciente: Paciente? = usuario.perfilPaciente
        if (paciente == null) {
            if ("PACIENTE".equals(usuario.rol, ignoreCase = true)) {
                logger.error("No se encontró el perfil de paciente para el usuario PACIENTE: $username")
                redirectAttributes.addFlashAttribute("errorMessage", "Perfil de paciente no encontrado. Contacte a soporte.")
                return "redirect:/dashboard-paciente"
            } else {
                logger.error("Lógica de carga para rol ${usuario.rol} no implementada o paciente no especificado.")
                redirectAttributes.addFlashAttribute("errorMessage", "Funcionalidad no disponible para su rol o paciente no especificado.")
                return "redirect:/" // O al dashboard del médico/admin si tienen otra lógica
            }
        }

        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor seleccione un archivo para cargar.")
            return "redirect:/dashboard-paciente"
        }

        try {
            paciente?.let { // Usar safe call y let para asegurar que paciente no es nulo
                imagenStorageService.store(file, it)
                redirectAttributes.addFlashAttribute(
                    "successMessage", // Cambiado para coincidir con el uso en dashboard-paciente.html
                    "Archivo cargado exitosamente: " + file.originalFilename
                )
            } ?: run {
                // Esto no debería ocurrir si la lógica anterior de paciente es correcta
                redirectAttributes.addFlashAttribute("errorMessage", "Error interno: Paciente no disponible para la carga.")
                return "redirect:/dashboard-paciente"
            }
        } catch (e: IOException) { // Captura IOException específicamente para errores de store
            logger.error("Error de E/S al cargar el archivo: " + file.originalFilename, e)
            redirectAttributes.addFlashAttribute(
                "errorMessage", // Cambiado para coincidir
                "Error de E/S al cargar el archivo: " + e.message
            )
        } catch (e: Exception) { // Captura genérica para otros errores inesperados
            logger.error("Error inesperado al cargar el archivo: " + file.originalFilename, e)
            redirectAttributes.addFlashAttribute(
                "errorMessage", // Cambiado para coincidir
                "Error inesperado al cargar el archivo: " + e.message
            )
        }
        // Redirigir a una URL que pueda mostrar los mensajes flash.
        // Si dashboard-paciente.html no los muestra directamente, podrías redirigir con parámetros
        // o asegurar que la página a la que rediriges (o la que la incluye) pueda leer FlashAttributes.
        return "redirect:/dashboard-paciente"
    }

    @GetMapping("/historial")
    fun mostrarHistorial(
        model: Model,
        authentication: Authentication?,
        redirectAttributes: RedirectAttributes
    ): String {
        if (authentication == null || !authentication.isAuthenticated) {
            return "redirect:/login"
        }
        val username = authentication.name
        val usuario: Usuario = usuarioRepositorio.findByUsuario(username)
            ?: run {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.")
                return "redirect:/login"
            }

        val paciente: Paciente = usuario.perfilPaciente
            ?: run {
                logger.warn("No se encontró el perfil de paciente para el usuario: $username al ver historial.")
                redirectAttributes.addFlashAttribute("errorMessage", "Perfil de paciente no encontrado para ver historial.")
                return if ("PACIENTE".equals(usuario.rol, ignoreCase = true)) "redirect:/dashboard-paciente" else "redirect:/"
            }

        val imagenes: List<ImagenLesion> = imagenLesionRepositorio.findByPacienteOrderByFechaSubidaDesc(paciente)
        model.addAttribute("imagenes", imagenes)
        return "historial-imagenes"
    }

    @GetMapping("/view/{filename:.+}")
    @ResponseBody // Añadido para asegurar que el contenido del archivo se escriba directamente en la respuesta
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
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"")
                    .body(resource)
            } else {
                logger.error("No se pudo leer el archivo: $filename")
                return ResponseEntity.notFound().build()
            }
        } catch (e: MalformedURLException) {
            logger.error("Error al formar la URL para el archivo: $filename", e)
            return ResponseEntity.badRequest().build()
        } catch (e: StorageFileNotFoundException) {
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
