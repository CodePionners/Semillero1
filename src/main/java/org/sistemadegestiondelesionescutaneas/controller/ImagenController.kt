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
    private lateinit var imagenStorageService: ImagenStorageService //

    @Autowired
    private lateinit var usuarioRepositorio: Usuariorepositorio //

    @Autowired
    private lateinit var imagenLesionRepositorio: ImagenLesionrepositorio //

    @Autowired
    private lateinit var pacienteRepositorio: Pacienterepositorio //


    @PostMapping("/upload")
    fun handleFileUpload(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(name = "pacienteIdentificacion", required = false) pacienteIdentificacion: String?,
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

        val isMedico = usuario.rol.equals("MEDICO", ignoreCase = true)
        val isPacienteRole = usuario.rol.equals("PACIENTE", ignoreCase = true)

        var errorRedirectUrl = if (isMedico) "/medico/imagenes/cargar-para-paciente" else "/imagenes/historial"
        val successRedirectUrl = if (isMedico) "/medico/imagenes/cargar-para-paciente" else "/imagenes/historial"

        val pacienteParaGuardar: Paciente?

        if (isMedico) {
            if (pacienteIdentificacion.isNullOrBlank()) {
                logger.warn("Médico $username intentó subir imagen sin especificar la identificación del paciente.")
                redirectAttributes.addFlashAttribute("errorUploadMessage", "Por favor, ingrese la identificación del paciente.")
                return "redirect:$errorRedirectUrl"
            }
            pacienteParaGuardar = pacienteRepositorio.findByIdentificacion(pacienteIdentificacion).orElse(null)
            if (pacienteParaGuardar == null) {
                logger.error("Médico $username intentó subir imagen para paciente con identificación '$pacienteIdentificacion' no encontrado.")
                redirectAttributes.addFlashAttribute("errorUploadMessage", "Paciente con identificación '$pacienteIdentificacion' no encontrado. Verifique la identificación.")
                return "redirect:$errorRedirectUrl"
            }
            logger.info("Médico $username subiendo imagen para paciente ID: ${pacienteParaGuardar.id} (Identificación: ${pacienteParaGuardar.identificacion}, Nombre: ${pacienteParaGuardar.nombre})")
        } else if (isPacienteRole) {
            pacienteParaGuardar = usuario.perfilPaciente
            if (pacienteParaGuardar == null) {
                logger.error("No se encontró el perfil de paciente para el usuario PACIENTE: $username")
                redirectAttributes.addFlashAttribute("errorUploadMessage", "Perfil de paciente no encontrado. Contacte a soporte.")
                return "redirect:$errorRedirectUrl"
            }
            logger.info("Paciente $username (ID: ${pacienteParaGuardar.id}) subiendo imagen para sí mismo.")
        } else {
            logger.error("Lógica de carga para rol ${usuario.rol} no implementada o paciente no especificado.")
            redirectAttributes.addFlashAttribute("errorUploadMessage", "Funcionalidad no disponible para su rol.")
            return "redirect:/"
        }

        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("errorUploadMessage", "Por favor seleccione un archivo para cargar.")
            return "redirect:$errorRedirectUrl"
        }

        try {
            imagenStorageService.store(file, pacienteParaGuardar)
            redirectAttributes.addFlashAttribute(
                "successUploadMessage",
                "Archivo cargado exitosamente para el paciente ${pacienteParaGuardar.nombre} (ID: ${pacienteParaGuardar.identificacion}): " + file.originalFilename
            )
        } catch (e: IOException) {
            logger.error("Error de E/S al cargar el archivo: " + file.originalFilename + " para paciente ID " + (pacienteParaGuardar?.id ?: "DESCONOCIDO"), e)
            redirectAttributes.addFlashAttribute(
                "errorUploadMessage",
                "Error de E/S al cargar el archivo: " + e.message
            )
            return "redirect:$errorRedirectUrl"
        } catch (e: Exception) {
            logger.error("Error inesperado al cargar el archivo: " + file.originalFilename + " para paciente ID " + (pacienteParaGuardar?.id ?: "DESCONOCIDO"), e)
            redirectAttributes.addFlashAttribute(
                "errorUploadMessage",
                "Error inesperado al cargar el archivo: " + e.message
            )
            return "redirect:$errorRedirectUrl"
        }
        return "redirect:$successRedirectUrl"
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
                redirectAttributes.addFlashAttribute("errorMessage", "Sesión inválida o usuario no encontrado.")
                return "redirect:/login"
            }

        if (!"PACIENTE".equals(usuario.rol, ignoreCase = true)) {
            logger.warn("Usuario ${username} con rol ${usuario.rol} intentó acceder a /imagenes/historial (restringido a PACIENTE).")
            redirectAttributes.addFlashAttribute("errorMessage", "Acceso no autorizado para esta vista directa. Los médicos deben acceder al historial a través de la lista de pacientes.")
            return if (usuario.rol.equals("MEDICO", ignoreCase = true)) "redirect:/medico/dashboard" else "redirect:/"
        }

        val paciente: Paciente? = usuario.perfilPaciente
        if (paciente == null) {
            logger.warn("No se encontró el perfil de paciente para el usuario: $username al ver historial.")
            model.addAttribute("errorMessage", "Perfil de paciente no encontrado. Contacte a soporte.")
            model.addAttribute("imagenes", emptyList<ImagenLesion>())
            return "historial-imagenes"
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
                    .body(resource)
            } else {
                logger.error("No se pudo leer el archivo o no existe: $filename")
                throw StorageFileNotFoundException("No se pudo leer el archivo: $filename")
            }
        } catch (e: MalformedURLException) {
            logger.error("Error al formar la URL para el archivo: $filename", e)
            return ResponseEntity.internalServerError().build()
        } catch (e: StorageFileNotFoundException) {
            logger.warn("Archivo no encontrado al servir: $filename", e)
            return ResponseEntity.notFound().build()
        } catch (e: IOException) {
            logger.error("Error de E/S al servir el archivo $filename", e)
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

        val redirectUrl = if (currentUser.rol.equals("MEDICO", ignoreCase = true)) {
            redirectAttributes.addFlashAttribute("infoMessage","Imagen eliminada. Redirigiendo a su vista principal.")
            // Si un médico elimina, idealmente se le debería redirigir a la página del paciente
            // o a su dashboard. Por ahora, si el médico tiene un perfil de paciente (poco común)
            // va a su historial, sino a su dashboard.
            if (currentUser.perfilPaciente != null && currentUser.perfilPaciente.id != null ) "/imagenes/historial" else "/medico/dashboard"
        } else {
            "/imagenes/historial"
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
        return "redirect:$redirectUrl"
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ImagenController::class.java)
    }
}