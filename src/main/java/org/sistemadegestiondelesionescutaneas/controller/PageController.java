package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.model.*;
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.sistemadegestiondelesionescutaneas.service.PacienteService;
import org.sistemadegestiondelesionescutaneas.service.HistorialExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Optional;

@Controller
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    @Autowired // Inyectar Usuariorepositorio para el PacientePageController
    private Usuariorepositorio usuarioRepositorio;

    @Autowired // Inyectar PacienteService para el PacientePageController
    private PacienteService pacienteService;

    @Autowired // Inyectar HistorialExportService para el PacientePageController
    private HistorialExportService historialExportService;


    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("loginError", "Usuario o contraseña incorrectos.");
        if (logout != null) model.addAttribute("logoutMessage", "Has cerrado sesión exitosamente.");
        return "login";
    }

    @GetMapping("/")
    public String homePage(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("requestURI", request.getRequestURI()); // Para la lógica de la pestaña activa
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority();
                if ("ROLE_PACIENTE".equals(role)) return "redirect:/paciente/historial"; // CAMBIADO
                if ("ROLE_MEDICO".equals(role)) return "redirect:/medico/dashboard";
                if ("ROLE_ADMIN".equals(role)) { model.addAttribute("requestURI", "/admin/dashboard"); return "dashboard-admin"; } // Asumiendo que tienes esta vista
            }
            // Si no tiene un rol manejado, redirigir a login con error
            return "redirect:/login?error=unauthorized_role";
        }
        // Si no está autenticado, redirigir a login
        return "redirect:/login";
    }

    // --- MedicoController se mantiene igual que en la versión anterior ---
    @Controller
    public static class MedicoController {
        private static final Logger medicoLogger = LoggerFactory.getLogger(MedicoController.class);

        private final PacienteService pacienteService;
        private final ImagenLesionrepositorio imagenLesionRepositorio;
        private final HistorialExportService historialExportService;

        @Autowired
        public MedicoController(PacienteService pacienteService,
                                ImagenLesionrepositorio imagenLesionRepositorio,
                                HistorialExportService historialExportService) {
            this.pacienteService = pacienteService;
            this.imagenLesionRepositorio = imagenLesionRepositorio;
            this.historialExportService = historialExportService;
        }

        @GetMapping("/medico/dashboard")
        public String medicoDashboard(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "dashboard-medico";
        }

        @GetMapping("/medico/galeria/ver-imagenes")
        public String medicoGaleriaVerImagenes(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            try {
                model.addAttribute("imagenesPrincipales", imagenLesionRepositorio.findAll());
            } catch (Exception e) {
                medicoLogger.error("Error al cargar imágenes para galería: {}", e.getMessage(), e);
                model.addAttribute("imagenesPrincipales", Collections.emptyList());
                model.addAttribute("errorMessageGalerias", "No se pudieron cargar las imágenes.");
            }
            return "medico-galeria-principal";
        }

        @GetMapping("/medico/pacientes/lista")
        public String medicoPacientesLista(
                @RequestParam(name = "identificacionParaBuscar", required = false) String identificacionParaBuscar,
                Model model, HttpServletRequest request) {
            String fullRequestUri = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            model.addAttribute("requestURI", fullRequestUri);

            Paciente pacienteEncontrado = new Paciente();

            if (identificacionParaBuscar != null && !identificacionParaBuscar.trim().isEmpty()) {
                try {
                    Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(identificacionParaBuscar.trim());
                    if (pacienteOpt.isPresent()) {
                        pacienteEncontrado = pacienteOpt.get();
                    } else {
                        model.addAttribute("searchMessage", "No se encontró paciente con identificación: " + identificacionParaBuscar);
                    }
                } catch (Exception e) {
                    medicoLogger.error("Error buscando paciente con identificación {}: {}", identificacionParaBuscar, e.getMessage(), e);
                    model.addAttribute("errorMessage", "Error al buscar paciente.");
                }
            }
            model.addAttribute("paciente", pacienteEncontrado);
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("edadesEstimadas", EdadEstimada.values());
            model.addAttribute("areasCorporales", AreaCorporalAfectada.values());
            model.addAttribute("tiposPiel", TipoPielFitzpatrick.values());
            model.addAttribute("tamanosLesion", TamanodeLesion.values());
            model.addAttribute("antecedentesCancer", AntecedentesFamiliaresCancer.values());
            model.addAttribute("identificacionBusquedaActual", identificacionParaBuscar);

            model.addAttribute("tiposReporte", TipoReporte.values());
            model.addAttribute("diagnosticos", Diagnostico.values());

            return "medico-pacientes-lista";
        }

        @PostMapping("/medico/pacientes/actualizar")
        public String medicoActualizarPaciente(@ModelAttribute("paciente") Paciente pacienteForm,
                                               RedirectAttributes redirectAttributes, HttpServletRequest request) {
            String identificacionOriginal = request.getParameter("identificacionOriginal");
            if (identificacionOriginal == null || identificacionOriginal.isEmpty()) {
                identificacionOriginal = pacienteForm.getIdentificacion();
            }

            String redirectUrl = "redirect:/medico/pacientes/lista" +
                    (identificacionOriginal != null && !identificacionOriginal.isEmpty() ?
                            "?identificacionParaBuscar=" + identificacionOriginal : "");

            if (pacienteForm.getId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: ID de paciente no hallado para actualizar.");
                return redirectUrl;
            }
            try {
                pacienteService.updatePaciente(pacienteForm);
                redirectAttributes.addFlashAttribute("successMessage", "Paciente '" + pacienteForm.getNombre() + "' actualizado y entrada de historial creada.");
            } catch (Exception e) {
                medicoLogger.error("Error al actualizar paciente ID {}: {}", pacienteForm.getId(), e.getMessage(), e);
                redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar: " + e.getMessage());
            }
            return redirectUrl;
        }

        @GetMapping("/medico/historial/consultas")
        public String medicoHistorialConsultas(
                @RequestParam(name = "idPaciente", required = false) String idPaciente,
                Model model, HttpServletRequest request) {

            String fullRequestUri = request.getRequestURI() + (idPaciente != null ? "?idPaciente=" + idPaciente : "");
            model.addAttribute("requestURI", fullRequestUri);

            medicoLogger.info("Accediendo a Historial Consultas para paciente con identificación: {}", idPaciente);

            if (idPaciente != null && !idPaciente.trim().isEmpty()) {
                Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(idPaciente.trim());
                if (pacienteOpt.isPresent()) {
                    Paciente paciente = pacienteOpt.get();
                    model.addAttribute("pacienteSeleccionado", paciente);
                    model.addAttribute("entradasHistorial", paciente.getHistorial());
                } else {
                    model.addAttribute("errorMessage", "Paciente con identificación '" + idPaciente + "' no encontrado.");
                    model.addAttribute("entradasHistorial", Collections.emptyList());
                    model.addAttribute("pacienteSeleccionado", null);
                }
            } else {
                model.addAttribute("infoMessage", "Para ver el historial, primero busque un paciente en la pestaña 'Pacientes' e ingrese su identificación en la URL de esta página (ej: .../consultas?idPaciente=IDENTIFICACION_DEL_PACIENTE) o use un enlace directo desde la página del paciente.");
                model.addAttribute("entradasHistorial", Collections.emptyList());
                model.addAttribute("pacienteSeleccionado", null);
            }
            return "medico-historial-consultas";
        }

        @GetMapping("/medico/historial/descargar/{pacienteId}/pdf")
        public ResponseEntity<InputStreamResource> descargarHistorialPdf(@PathVariable("pacienteId") Long pacienteIdObject) {
            medicoLogger.info("Solicitud de descarga de historial PDF para paciente con ID de BD: {}", pacienteIdObject);
            Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByDbId(pacienteIdObject);

            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                medicoLogger.info("Generando PDF para paciente: {}, Identificación: {}", paciente.getNombre(), paciente.getIdentificacion());

                ByteArrayInputStream bis = historialExportService.generarHistorialPdf(paciente);
                if (bis != null) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "inline; filename=historial_paciente_" + paciente.getIdentificacion() + ".pdf");
                    return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
                } else {
                    medicoLogger.error("El servicio de exportación de PDF devolvió null inesperadamente para el paciente ID {}", pacienteIdObject);
                    return ResponseEntity.internalServerError().body(null);
                }
            }
            medicoLogger.warn("No se pudo generar PDF para paciente ID {} (no encontrado).", pacienteIdObject);
            return ResponseEntity.notFound().build();
        }


        @GetMapping("/medico/historial/descargar/{pacienteId}/csv")
        public ResponseEntity<InputStreamResource> descargarHistorialCsv(@PathVariable("pacienteId") Long pacienteIdObject) {
            medicoLogger.info("Solicitud de descarga de historial CSV para paciente con ID de BD: {}", pacienteIdObject);

            Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByDbId(pacienteIdObject);

            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                medicoLogger.info("Generando CSV para paciente: {}, Identificación: {}", paciente.getNombre(), paciente.getIdentificacion());
                try {
                    ByteArrayInputStream bis = historialExportService.generarHistorialCsv(paciente);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "attachment; filename=historial_paciente_" + paciente.getIdentificacion() + ".csv");
                    return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("text/csv")).body(new InputStreamResource(bis));
                } catch (Exception e) {
                    medicoLogger.error("Error al generar CSV para paciente ID {}: {}", pacienteIdObject, e.getMessage(), e);
                    return ResponseEntity.internalServerError().body(null);
                }
            }
            medicoLogger.warn("No se pudo generar CSV para paciente ID {} (no encontrado).", pacienteIdObject);
            return ResponseEntity.notFound().build();
        }

        @GetMapping("/medico/imagenes/cargar-para-paciente")
        public String medicoCargarImagen(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            model.addAttribute("dashboardReturnUrl", "/medico/dashboard");
            model.addAttribute("userRole", "MEDICO");
            return "cargar-imagen";
        }
        @GetMapping("/medico/galeria/info-general")
        public String medicoGaleriaInfoGeneral(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "forward:/medico/dashboard";
        }
        @GetMapping("/medico/pacientes/agregar")
        public String medicoAgregarPacienteForm(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "redirect:/medico/pacientes/lista";
        }
    }

    // --- NUEVO PacientePageController ---
    @Controller
    @RequestMapping("/paciente") // Prefijo para todas las rutas de este controlador
    public class PacientePageController {
        private static final Logger pacienteLogger = LoggerFactory.getLogger(PacientePageController.class);

        // Inyectar los servicios necesarios (ya están disponibles en la clase PageController externa)
        // Se usa la instancia de la clase externa para acceder a ellos.

        @GetMapping("/historial")
        public String pacienteHistorial(Authentication authentication, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            String username = authentication.getName();
            Usuario usuario = PageController.this.usuarioRepositorio.findByUsuario(username); // Acceder a través de la instancia de PageController

            if (usuario == null || usuario.getPerfilPaciente() == null) {
                pacienteLogger.warn("Usuario {} no encontrado o sin perfil de paciente.", username);
                redirectAttributes.addFlashAttribute("errorMessage", "No se encontró su perfil de paciente.");
                return "redirect:/login"; // O a una página de error/dashboard genérico
            }

            Paciente paciente = usuario.getPerfilPaciente();
            // Cargar el paciente completo con su historial
            Optional<Paciente> pacienteCompletoOpt = PageController.this.pacienteService.findPacienteCompletoByDbId(paciente.getId());

            if (pacienteCompletoOpt.isPresent()) {
                model.addAttribute("pacienteSeleccionado", pacienteCompletoOpt.get());
                model.addAttribute("entradasHistorial", pacienteCompletoOpt.get().getHistorial());
            } else {
                // Esto sería inesperado si el perfil del paciente existe
                pacienteLogger.error("No se pudo cargar el perfil completo del paciente ID {} para el usuario {}", paciente.getId(), username);
                model.addAttribute("errorMessage", "Error al cargar su historial.");
                model.addAttribute("entradasHistorial", Collections.emptyList());
                model.addAttribute("pacienteSeleccionado", null);
            }

            model.addAttribute("requestURI", request.getRequestURI()); // Para la lógica de la pestaña activa
            return "dashboard-paciente-historial"; // Nueva plantilla para el historial del paciente
        }

        @GetMapping("/historial/descargar/{pacienteId}/pdf")
        public ResponseEntity<InputStreamResource> descargarHistorialPdf(
                @PathVariable("pacienteId") Long pacienteIdDescarga,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            pacienteLogger.info("Solicitud de descarga de historial PDF para paciente con ID de BD: {} por usuario {}", pacienteIdDescarga, authentication.getName());

            Usuario usuarioAutenticado = PageController.this.usuarioRepositorio.findByUsuario(authentication.getName());
            if (usuarioAutenticado == null || usuarioAutenticado.getPerfilPaciente() == null) {
                pacienteLogger.warn("Intento de descarga de PDF por usuario no válido o sin perfil: {}", authentication.getName());
                return ResponseEntity.status(403).build(); // Forbidden
            }

            // Verificar que el paciente autenticado solo descargue su propio historial
            if (!usuarioAutenticado.getPerfilPaciente().getId().equals(pacienteIdDescarga)) {
                pacienteLogger.warn("Intento de descarga de PDF no autorizado. Usuario {} intentó descargar historial de paciente ID {}", authentication.getName(), pacienteIdDescarga);
                throw new AccessDeniedException("No tiene permiso para descargar el historial de este paciente.");
            }

            Optional<Paciente> pacienteOpt = PageController.this.pacienteService.findPacienteCompletoByDbId(pacienteIdDescarga);

            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                pacienteLogger.info("Generando PDF para paciente: {}, Identificación: {}", paciente.getNombre(), paciente.getIdentificacion());
                ByteArrayInputStream bis = PageController.this.historialExportService.generarHistorialPdf(paciente);
                if (bis != null) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "inline; filename=mi_historial_" + paciente.getIdentificacion() + ".pdf");
                    return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
                } else {
                    pacienteLogger.error("El servicio de exportación de PDF devolvió null para el paciente ID {}", pacienteIdDescarga);
                    return ResponseEntity.internalServerError().build();
                }
            }
            pacienteLogger.warn("No se pudo generar PDF para paciente ID {} (no encontrado).", pacienteIdDescarga);
            return ResponseEntity.notFound().build();
        }

        @GetMapping("/historial/descargar/{pacienteId}/csv")
        public ResponseEntity<InputStreamResource> descargarHistorialCsv(
                @PathVariable("pacienteId") Long pacienteIdDescarga,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            pacienteLogger.info("Solicitud de descarga de historial CSV para paciente con ID de BD: {} por usuario {}", pacienteIdDescarga, authentication.getName());

            Usuario usuarioAutenticado = PageController.this.usuarioRepositorio.findByUsuario(authentication.getName());
            if (usuarioAutenticado == null || usuarioAutenticado.getPerfilPaciente() == null) {
                pacienteLogger.warn("Intento de descarga de CSV por usuario no válido o sin perfil: {}", authentication.getName());
                return ResponseEntity.status(403).build(); // Forbidden
            }

            if (!usuarioAutenticado.getPerfilPaciente().getId().equals(pacienteIdDescarga)) {
                pacienteLogger.warn("Intento de descarga de CSV no autorizado. Usuario {} intentó descargar historial de paciente ID {}", authentication.getName(), pacienteIdDescarga);
                throw new AccessDeniedException("No tiene permiso para descargar el historial de este paciente.");
            }

            Optional<Paciente> pacienteOpt = PageController.this.pacienteService.findPacienteCompletoByDbId(pacienteIdDescarga);

            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                pacienteLogger.info("Generando CSV para paciente: {}, Identificación: {}", paciente.getNombre(), paciente.getIdentificacion());
                try {
                    ByteArrayInputStream bis = PageController.this.historialExportService.generarHistorialCsv(paciente);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "attachment; filename=mi_historial_" + paciente.getIdentificacion() + ".csv");
                    return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("text/csv")).body(new InputStreamResource(bis));
                } catch (Exception e) {
                    pacienteLogger.error("Error al generar CSV para paciente ID {}: {}", pacienteIdDescarga, e.getMessage(), e);
                    return ResponseEntity.internalServerError().build();
                }
            }
            pacienteLogger.warn("No se pudo generar CSV para paciente ID {} (no encontrado).", pacienteIdDescarga);
            return ResponseEntity.notFound().build();
        }
        @GetMapping("/imagenes/cargar") // Ruta para que el paciente cargue sus imágenes
        public String pacienteCargarImagen(Model model, HttpServletRequest request, Authentication authentication) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            model.addAttribute("requestURI", request.getRequestURI());
            model.addAttribute("dashboardReturnUrl", "/paciente/historial"); // Volver al nuevo historial del paciente
            model.addAttribute("userRole", "PACIENTE");
            return "cargar-imagen";
        }
    }
}
