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

    @Autowired
    private Usuariorepositorio usuarioRepositorio;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
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
        model.addAttribute("requestURI", request.getRequestURI());
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority();
                if ("ROLE_PACIENTE".equals(role)) return "redirect:/paciente/historial";
                if ("ROLE_MEDICO".equals(role)) return "redirect:/medico/dashboard";
                if ("ROLE_ADMIN".equals(role)) { model.addAttribute("requestURI", "/admin/dashboard"); return "dashboard-admin"; }
            }
            return "redirect:/login?error=unauthorized_role";
        }
        return "redirect:/login";
    }

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
                // Considera la paginación si hay muchas imágenes
                model.addAttribute("imagenesPrincipales", imagenLesionRepositorio.findAllWithPacienteOrderByFechaSubidaDesc()); // Usar método específico
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

            Paciente pacienteEncontrado = null; // Cambiado a null para una verificación más clara

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
            // Si pacienteEncontrado sigue siendo null, Thymeleaf tratará sus campos como null, lo cual es manejable.
            // O puedes añadir un objeto Paciente vacío si prefieres para evitar errores de null en la plantilla si no se manejan.
            model.addAttribute("paciente", pacienteEncontrado != null ? pacienteEncontrado : new Paciente());
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
                @RequestParam(name = "idPaciente", required = false) String identificacionPaciente, // Renombrado para claridad que es la identificación
                Model model, HttpServletRequest request) {

            String fullRequestUri = request.getRequestURI() + (identificacionPaciente != null ? "?idPaciente=" + identificacionPaciente : "");
            model.addAttribute("requestURI", fullRequestUri);

            medicoLogger.info("Médico accediendo a Historial Consultas para paciente con IDENTIFICACIÓN: {}", identificacionPaciente);

            if (identificacionPaciente != null && !identificacionPaciente.trim().isEmpty()) {
                Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(identificacionPaciente.trim());
                if (pacienteOpt.isPresent()) {
                    Paciente paciente = pacienteOpt.get();
                    model.addAttribute("pacienteSeleccionado", paciente);
                    model.addAttribute("entradasHistorial", paciente.getHistorial());
                    medicoLogger.info("Historial cargado para médico. Paciente: '{}' (ID BD: {}), Identificación: '{}'. Número de entradas: {}",
                            paciente.getNombre(), paciente.getId(), paciente.getIdentificacion(),
                            paciente.getHistorial() != null ? paciente.getHistorial().size() : "null");
                } else {
                    medicoLogger.warn("Paciente con IDENTIFICACIÓN '{}' no encontrado por el médico.", identificacionPaciente);
                    model.addAttribute("errorMessage", "Paciente con identificación '" + identificacionPaciente + "' no encontrado.");
                    model.addAttribute("entradasHistorial", Collections.emptyList());
                    model.addAttribute("pacienteSeleccionado", null);
                }
            } else {
                model.addAttribute("infoMessage", "Para ver el historial, primero busque un paciente usando su identificación.");
                model.addAttribute("entradasHistorial", Collections.emptyList());
                model.addAttribute("pacienteSeleccionado", null);
            }
            return "medico-historial-consultas";
        }

        @GetMapping("/medico/historial/descargar/{pacienteId}/pdf")
        public ResponseEntity<InputStreamResource> descargarHistorialPdf(@PathVariable("pacienteId") Long pacienteDbId) { // Renombrado para claridad que es ID de BD
            medicoLogger.info("Médico solicitando descarga PDF del historial para paciente con ID de BD: {}", pacienteDbId);
            Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByDbId(pacienteDbId);

            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                medicoLogger.info("Generando PDF para paciente: {}, Identificación: {}", paciente.getNombre(), paciente.getIdentificacion());
                ByteArrayInputStream bis = historialExportService.generarHistorialPdf(paciente);
                if (bis != null) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "inline; filename=historial_paciente_" + paciente.getIdentificacion() + ".pdf");
                    return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
                } else {
                    medicoLogger.error("El servicio de exportación PDF devolvió null para paciente ID de BD {}", pacienteDbId);
                    return ResponseEntity.internalServerError().build();
                }
            }
            medicoLogger.warn("No se pudo generar PDF. Paciente con ID de BD {} no encontrado.", pacienteDbId);
            return ResponseEntity.notFound().build();
        }

        @GetMapping("/medico/historial/descargar/{pacienteId}/csv")
        public ResponseEntity<InputStreamResource> descargarHistorialCsv(@PathVariable("pacienteId") Long pacienteDbId) { // ID de BD
            medicoLogger.info("Médico solicitando descarga CSV del historial para paciente con ID de BD: {}", pacienteDbId);
            Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByDbId(pacienteDbId);

            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                medicoLogger.info("Generando CSV para paciente: {}, Identificación: {}", paciente.getNombre(), paciente.getIdentificacion());
                try {
                    ByteArrayInputStream bis = historialExportService.generarHistorialCsv(paciente);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "attachment; filename=historial_paciente_" + paciente.getIdentificacion() + ".csv");
                    return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("text/csv")).body(new InputStreamResource(bis));
                } catch (Exception e) {
                    medicoLogger.error("Error al generar CSV para paciente ID de BD {}: {}", pacienteDbId, e.getMessage(), e);
                    return ResponseEntity.internalServerError().build();
                }
            }
            medicoLogger.warn("No se pudo generar CSV. Paciente con ID de BD {} no encontrado.", pacienteDbId);
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

    @Controller
    @RequestMapping("/paciente")
    public class PacientePageController {
        private static final Logger pacienteLogger = LoggerFactory.getLogger(PacientePageController.class);

        @GetMapping("/historial")
        public String pacienteHistorial(Authentication authentication, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
            pacienteLogger.info("Accediendo a /paciente/historial");
            if (authentication == null || !authentication.isAuthenticated()) {
                pacienteLogger.warn("Intento de acceso a /paciente/historial sin autenticación.");
                return "redirect:/login";
            }

            String username = authentication.getName();
            pacienteLogger.info("Usuario autenticado: {}", username);
            Usuario usuario = PageController.this.usuarioRepositorio.findByUsuario(username);

            if (usuario == null) {
                pacienteLogger.error("CRÍTICO: Usuario autenticado '{}' NO FUE ENCONTRADO en la base de datos.", username);
                redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación crítico: Su usuario no fue encontrado. Por favor, inicie sesión de nuevo.");
                return "redirect:/login";
            }
            pacienteLogger.info("Usuario encontrado en BD: ID {}, Nombre: {}", usuario.getId(), usuario.getNombre());

            if (usuario.getPerfilPaciente() == null) {
                pacienteLogger.warn("El usuario '{}' (ID Usuario: {}) NO TIENE un perfil de paciente asociado. No se puede mostrar el historial.", username, usuario.getId());
                model.addAttribute("errorMessage", "No se encontró un perfil de paciente asociado a su cuenta. Si cree que esto es un error, por favor contacte a soporte.");
                model.addAttribute("pacienteSeleccionado", null);
                model.addAttribute("entradasHistorial", Collections.emptyList());
                model.addAttribute("requestURI", request.getRequestURI());
                return "dashboard-paciente-historial";
            }

            Paciente pacienteDelPerfil = usuario.getPerfilPaciente();
            pacienteLogger.info("Perfil de paciente encontrado para el usuario '{}': ID Paciente {}, Nombre Paciente: {}, Identificación Paciente: {}",
                    username, pacienteDelPerfil.getId(), pacienteDelPerfil.getNombre(), pacienteDelPerfil.getIdentificacion());

            // Volver a cargar el paciente completo desde el servicio para asegurar que todas las colecciones EAGER (como historial) estén frescas
            // y para obtener la versión más actualizada desde la base de datos.
            Optional<Paciente> pacienteCompletoOpt = PageController.this.pacienteService.findPacienteCompletoByDbId(pacienteDelPerfil.getId());

            if (pacienteCompletoOpt.isPresent()) {
                Paciente pacienteConHistorial = pacienteCompletoOpt.get();
                pacienteLogger.info("Paciente completo (ID BD: {}) cargado para la vista del historial. Nombre: '{}', Identificación: '{}'. Número de entradas de historial encontradas: {}",
                        pacienteConHistorial.getId(),
                        pacienteConHistorial.getNombre(),
                        pacienteConHistorial.getIdentificacion(),
                        pacienteConHistorial.getHistorial() != null ? pacienteConHistorial.getHistorial().size() : "NULL_COLLECTION");

                model.addAttribute("pacienteSeleccionado", pacienteConHistorial);
                model.addAttribute("entradasHistorial", pacienteConHistorial.getHistorial());

                if (pacienteConHistorial.getHistorial() == null || pacienteConHistorial.getHistorial().isEmpty()) {
                    pacienteLogger.warn(">>>> La lista 'entradasHistorial' para el paciente ID BD {} ({}, Identificación: {}) está vacía o es nula DESPUÉS de la carga completa desde el servicio. <<<<",
                            pacienteConHistorial.getId(), pacienteConHistorial.getNombre(), pacienteConHistorial.getIdentificacion());
                }

            } else {
                pacienteLogger.error("CRÍTICO: No se pudo cargar el objeto Paciente completo desde la base de datos para el ID Paciente {} (asociado al usuario '{}', Identificación Paciente del perfil: {}). Esto indica un problema grave si el perfil del paciente existía.",
                        pacienteDelPerfil.getId(), username, pacienteDelPerfil.getIdentificacion());
                model.addAttribute("errorMessage", "Error crítico al intentar cargar su historial completo. Su perfil de paciente podría estar inconsistente. Por favor, contacte a soporte.");
                model.addAttribute("entradasHistorial", Collections.emptyList());
                model.addAttribute("pacienteSeleccionado", pacienteDelPerfil); // Mostrar datos básicos del perfil si es posible
            }

            model.addAttribute("requestURI", request.getRequestURI());
            return "dashboard-paciente-historial";
        }

        @GetMapping("/historial/descargar/{pacienteId}/pdf")
        public ResponseEntity<InputStreamResource> descargarHistorialPdf(
                @PathVariable("pacienteId") Long pacienteIdDescarga, // ID de BD del paciente
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            pacienteLogger.info("Paciente {} solicitando descarga PDF del historial para paciente con ID de BD: {}", authentication.getName(), pacienteIdDescarga);

            Usuario usuarioAutenticado = PageController.this.usuarioRepositorio.findByUsuario(authentication.getName());
            if (usuarioAutenticado == null || usuarioAutenticado.getPerfilPaciente() == null) {
                pacienteLogger.warn("Intento de descarga PDF por usuario no válido o sin perfil: {}", authentication.getName());
                return ResponseEntity.status(403).build(); // Forbidden
            }

            if (!usuarioAutenticado.getPerfilPaciente().getId().equals(pacienteIdDescarga)) {
                pacienteLogger.warn("Intento de descarga PDF NO AUTORIZADO. Usuario {} (Paciente ID {}) intentó descargar historial de Paciente ID {}",
                        authentication.getName(), usuarioAutenticado.getPerfilPaciente().getId(), pacienteIdDescarga);
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
                    pacienteLogger.error("El servicio de exportación PDF devolvió null para paciente ID de BD {}", pacienteIdDescarga);
                    return ResponseEntity.internalServerError().build();
                }
            }
            pacienteLogger.warn("No se pudo generar PDF. Paciente con ID de BD {} no encontrado.", pacienteIdDescarga);
            return ResponseEntity.notFound().build();
        }

        @GetMapping("/historial/descargar/{pacienteId}/csv")
        public ResponseEntity<InputStreamResource> descargarHistorialCsv(
                @PathVariable("pacienteId") Long pacienteIdDescarga, // ID de BD del paciente
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

            pacienteLogger.info("Paciente {} solicitando descarga CSV del historial para paciente con ID de BD: {}", authentication.getName(), pacienteIdDescarga);

            Usuario usuarioAutenticado = PageController.this.usuarioRepositorio.findByUsuario(authentication.getName());
            if (usuarioAutenticado == null || usuarioAutenticado.getPerfilPaciente() == null) {
                pacienteLogger.warn("Intento de descarga CSV por usuario no válido o sin perfil: {}", authentication.getName());
                return ResponseEntity.status(403).build(); // Forbidden
            }

            if (!usuarioAutenticado.getPerfilPaciente().getId().equals(pacienteIdDescarga)) {
                pacienteLogger.warn("Intento de descarga CSV NO AUTORIZADO. Usuario {} (Paciente ID {}) intentó descargar historial de Paciente ID {}",
                        authentication.getName(), usuarioAutenticado.getPerfilPaciente().getId(), pacienteIdDescarga);
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
                    pacienteLogger.error("Error al generar CSV para paciente ID de BD {}: {}", pacienteIdDescarga, e.getMessage(), e);
                    return ResponseEntity.internalServerError().build();
                }
            }
            pacienteLogger.warn("No se pudo generar CSV. Paciente con ID de BD {} no encontrado.", pacienteIdDescarga);
            return ResponseEntity.notFound().build();
        }

        @GetMapping("/imagenes/cargar")
        public String pacienteCargarImagen(Model model, HttpServletRequest request, Authentication authentication) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            model.addAttribute("requestURI", request.getRequestURI());
            model.addAttribute("dashboardReturnUrl", "/paciente/historial");
            model.addAttribute("userRole", "PACIENTE");
            return "cargar-imagen";
        }
    }
}