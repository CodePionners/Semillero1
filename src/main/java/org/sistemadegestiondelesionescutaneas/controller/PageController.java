package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.model.ImagenLesion;
import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.sistemadegestiondelesionescutaneas.model.Sexo;
import org.sistemadegestiondelesionescutaneas.model.EdadEstimada;
import org.sistemadegestiondelesionescutaneas.model.AreaCorporalAfectada;
import org.sistemadegestiondelesionescutaneas.model.TipoPielFitzpatrick;
import org.sistemadegestiondelesionescutaneas.model.TamanodeLesion;
import org.sistemadegestiondelesionescutaneas.model.AntecedentesFamiliaresCancer;
import org.sistemadegestiondelesionescutaneas.model.EntradaHistorial; // Para el historial
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio;
import org.sistemadegestiondelesionescutaneas.service.PacienteService;
// import org.sistemadegestiondelesionescutaneas.service.HistorialService; // Si creas un servicio para el historial
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Para la descarga
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable; // Para el pacienteId en la URL
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections; // Para lista vacía
import java.util.List;
import java.util.Optional;

@Controller
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Has cerrado sesión exitosamente.");
        }
        return "login";
    }

    @GetMapping("/")
    public String homePage(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("requestURI", request.getRequestURI());

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            String username = authentication.getName();
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority();
                if ("ROLE_PACIENTE".equals(role)) return "redirect:/imagenes/historial";
                if ("ROLE_MEDICO".equals(role)) return "redirect:/medico/dashboard";
                if ("ROLE_ADMIN".equals(role)) {
                    model.addAttribute("requestURI", "/admin/dashboard");
                    return "dashboard-admin";
                }
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
        // private final HistorialService historialService; // Inyectar si se crea

        @Autowired
        public MedicoController(PacienteService pacienteService,
                                ImagenLesionrepositorio imagenLesionRepositorio
                /*, HistorialService historialService */) {
            this.pacienteService = pacienteService;
            this.imagenLesionRepositorio = imagenLesionRepositorio;
            // this.historialService = historialService;
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
                List<ImagenLesion> todasLasImagenes = imagenLesionRepositorio.findAll();
                model.addAttribute("imagenesPrincipales", todasLasImagenes);
            } catch (Exception e) {
                medicoLogger.error("Error al cargar imágenes para la galería: {}", e.getMessage(), e);
                model.addAttribute("imagenesPrincipales", Collections.emptyList());
                model.addAttribute("errorMessageGalerias", "No se pudieron cargar las imágenes.");
            }
            return "medico-galeria-principal";
        }

        @GetMapping("/medico/pacientes/lista")
        public String medicoPacientesLista(
                @RequestParam(name = "identificacionParaBuscar", required = false) String identificacionParaBuscar,
                Model model,
                HttpServletRequest request) {

            String requestURI = request.getRequestURI();
            String fullRequestUri = requestURI + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            model.addAttribute("requestURI", fullRequestUri);

            Paciente pacienteEncontrado = new Paciente(); // Objeto vacío por defecto
            if (identificacionParaBuscar != null && !identificacionParaBuscar.trim().isEmpty()) {
                try {
                    Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(identificacionParaBuscar.trim());
                    if (pacienteOpt.isPresent()) {
                        pacienteEncontrado = pacienteOpt.get();
                    } else {
                        model.addAttribute("searchMessage", "No se encontró paciente con la identificación: " + identificacionParaBuscar);
                    }
                } catch (Exception e) {
                    medicoLogger.error("Error al buscar paciente ID {}: {}", identificacionParaBuscar, e.getMessage(), e);
                    model.addAttribute("errorMessage", "Error al buscar el paciente.");
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

            return "medico-pacientes-lista";
        }

        @PostMapping("/medico/pacientes/actualizar")
        public String medicoActualizarPaciente(@ModelAttribute("paciente") Paciente pacienteForm,
                                               RedirectAttributes redirectAttributes,
                                               HttpServletRequest request) {
            String identificacionOriginal = request.getParameter("identificacionOriginal");
            if (identificacionOriginal == null || identificacionOriginal.isEmpty()) {
                identificacionOriginal = pacienteForm.getIdentificacion();
            }
            String redirectUrl = "redirect:/medico/pacientes/lista" +
                    (identificacionOriginal != null && !identificacionOriginal.isEmpty() ?
                            "?identificacionParaBuscar=" + identificacionOriginal : "");

            if (pacienteForm.getId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: ID de paciente no encontrado para actualizar.");
                return redirectUrl;
            }
            try {
                pacienteService.updatePaciente(pacienteForm);
                redirectAttributes.addFlashAttribute("successMessage", "Paciente '" + pacienteForm.getNombre() + "' actualizado.");
            } catch (Exception e) {
                medicoLogger.error("Error al actualizar paciente ID {}: {}", pacienteForm.getId(), e.getMessage(), e);
                redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar paciente: " + e.getMessage());
            }
            return redirectUrl;
        }

        // Mappings de Reportes eliminados:
        // @GetMapping("/medico/reportes/generar") ...
        // @GetMapping("/medico/reportes/ver-generados") ...

        @GetMapping("/medico/historial/consultas")
        public String medicoHistorialConsultas(
                @RequestParam(name = "pacienteId", required = false) Long pacienteId, // Opcional, si se busca historial de un paciente específico
                Model model,
                HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            medicoLogger.info("Accessing Medico Historial Consultas. Paciente ID: {}", pacienteId);

            if (pacienteId != null) {
                Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(String.valueOf(pacienteId)); // O un método findById si la ID es Long
                if (pacienteOpt.isPresent()) {
                    model.addAttribute("pacienteSeleccionado", pacienteOpt.get());
                    model.addAttribute("entradasHistorial", pacienteOpt.get().getHistorial()); // Asumiendo que 'historial' se carga con EAGER o en el servicio
                } else {
                    model.addAttribute("errorMessage", "Paciente no encontrado para mostrar historial.");
                    model.addAttribute("entradasHistorial", Collections.emptyList());
                }
            } else {
                // Mostrar tabla vacía o un mensaje para buscar un paciente primero
                model.addAttribute("entradasHistorial", Collections.emptyList());
                // model.addAttribute("infoMessage", "Busque y seleccione un paciente para ver su historial detallado.");
            }
            return "medico-historial-consultas";
        }

        // Placeholder para la descarga del historial
        @GetMapping("/medico/historial/descargar/{pacienteId}")
        public ResponseEntity<String> descargarHistorial(@PathVariable("pacienteId") Long pacienteId, RedirectAttributes redirectAttributes) {
            medicoLogger.info("Solicitud de descarga de historial para paciente ID: {} (FUNCIONALIDAD PRÓXIMAMENTE)", pacienteId);
            // Aquí iría la lógica para generar el archivo PDF/CSV del historial del paciente.
            // Por ahora, es un placeholder.
            // redirectAttributes.addFlashAttribute("infoMessage", "La descarga de historial estará disponible próximamente.");
            // return "redirect:/medico/historial/consultas?pacienteId=" + pacienteId;
            // O si quieres devolver un archivo de texto simple como ejemplo:
            Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(String.valueOf(pacienteId)); // O findById(pacienteId)
            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                StringBuilder sb = new StringBuilder();
                sb.append("HISTORIAL CLÍNICO - PACIENTE\n");
                sb.append("---------------------------------\n");
                sb.append("Nombre: ").append(paciente.getNombre()).append("\n");
                sb.append("Identificación: ").append(paciente.getIdentificacion()).append("\n");
                sb.append("Edad: ").append(paciente.getEdad() != null ? paciente.getEdad() : "N/A").append("\n");
                sb.append("Sexo: ").append(paciente.getSexo() != null ? paciente.getSexo().getDescripcion() : "N/A").append("\n\n");
                sb.append("Entradas del Historial:\n");
                if (paciente.getHistorial() != null && !paciente.getHistorial().isEmpty()) {
                    for (EntradaHistorial entrada : paciente.getHistorial()) {
                        sb.append("  Fecha: ").append(entrada.getFechaHora() != null ? entrada.getFechaHora().toString() : "N/A").append("\n");
                        sb.append("  Evento: ").append(entrada.getEvento()).append("\n");
                        sb.append("  Detalles: ").append(entrada.getDetalles()).append("\n");
                        sb.append("  Estado: ").append(entrada.getEstado()).append("\n\n");
                    }
                } else {
                    sb.append("No hay entradas en el historial.\n");
                }
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"historial_paciente_" + paciente.getIdentificacion() + ".txt\"")
                        .body(sb.toString());
            }
            return ResponseEntity.notFound().build();
        }


        // Otros métodos del MedicoController que ya tenías (cargar imagen, etc.)
        @GetMapping("/medico/imagenes/cargar-para-paciente")
        public String medicoCargarImagen(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            model.addAttribute("dashboardReturnUrl", "/medico/dashboard");
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
            return "medico-pacientes-lista"; // O redirigir a una vista de agregar si la tienes
        }
    }
}