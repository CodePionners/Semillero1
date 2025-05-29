package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.model.*;
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio;
import org.sistemadegestiondelesionescutaneas.service.PacienteService;
import org.sistemadegestiondelesionescutaneas.service.HistorialExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

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
                if ("ROLE_PACIENTE".equals(role)) return "redirect:/imagenes/historial";
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
                    medicoLogger.error("Error buscando paciente ID {}: {}", identificacionParaBuscar, e.getMessage(), e);
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
            model.addAttribute("requestURI", request.getRequestURI() + (idPaciente != null ? "?idPaciente=" + idPaciente : ""));
            medicoLogger.info("Accediendo a Historial Consultas para paciente con identificación: {}", idPaciente);

            if (idPaciente != null && !idPaciente.trim().isEmpty()) {
                Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(idPaciente.trim());
                if (pacienteOpt.isPresent()) {
                    Paciente paciente = pacienteOpt.get();
                    model.addAttribute("pacienteSeleccionado", paciente);
                    // Con EAGER global, paciente.getHistorial() debería estar cargado.
                    model.addAttribute("entradasHistorial", paciente.getHistorial());
                } else {
                    model.addAttribute("errorMessage", "Paciente con identificación '" + idPaciente + "' no encontrado.");
                    model.addAttribute("entradasHistorial", Collections.emptyList());
                }
            } else {
                model.addAttribute("infoMessage", "Para ver el historial, primero busque un paciente en la pestaña 'Pacientes' e ingrese su identificación en la URL de esta página (ej: .../consultas?idPaciente=IDENTIFICACION).");
                model.addAttribute("entradasHistorial", Collections.emptyList());
            }
            return "medico-historial-consultas";
        }

        @GetMapping("/medico/historial/descargar/{pacienteId}/pdf")
        public ResponseEntity<InputStreamResource> descargarHistorialPdf(@PathVariable("pacienteId") Long pacienteId) {
            medicoLogger.info("Solicitud de descarga de historial PDF para paciente ID: {}", pacienteId);
            Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(String.valueOf(pacienteId));

            if (pacienteOpt.isPresent()) {
                ByteArrayInputStream bis = historialExportService.generarHistorialPdf(pacienteOpt.get());
                if (bis != null) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "inline; filename=historial_paciente_" + pacienteOpt.get().getIdentificacion() + ".pdf");
                    return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
                } else {
                    medicoLogger.error("El servicio de exportación de PDF devolvió null para el paciente ID {}", pacienteId);
                    return ResponseEntity.internalServerError().body(null); // O una respuesta de error más específica
                }
            }
            medicoLogger.warn("No se pudo generar PDF para paciente ID {} (no encontrado).", pacienteId);
            return ResponseEntity.notFound().build();
        }

        @GetMapping("/medico/historial/descargar/{pacienteId}/csv")
        public ResponseEntity<InputStreamResource> descargarHistorialCsv(@PathVariable("pacienteId") Long pacienteId) {
            medicoLogger.info("Solicitud de descarga de historial CSV para paciente ID: {}", pacienteId);
            Optional<Paciente> pacienteOpt = pacienteService.findPacienteCompletoByIdentificacion(String.valueOf(pacienteId));

            if (pacienteOpt.isPresent()) {
                try {
                    ByteArrayInputStream bis = historialExportService.generarHistorialCsv(pacienteOpt.get());
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "attachment; filename=historial_paciente_" + pacienteOpt.get().getIdentificacion() + ".csv");
                    return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("text/csv")).body(new InputStreamResource(bis));
                } catch (RuntimeException e) {
                    medicoLogger.error("Error al generar CSV para paciente ID {}: {}", pacienteId, e.getMessage(), e);
                    return ResponseEntity.internalServerError().body(null);
                }
            }
            medicoLogger.warn("No se pudo generar CSV para paciente ID {} (no encontrado).", pacienteId);
            return ResponseEntity.notFound().build();
        }

        // Otros métodos del MedicoController
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
            return "redirect:/medico/pacientes/lista";
        }
    }
}