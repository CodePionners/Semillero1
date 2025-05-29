package org.sistemadegestiondelesionescutaneas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            logger.warn("Login attempt failed, error parameter is present. Displaying error message.");
            model.addAttribute("loginError", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            logger.info("User logged out successfully. Displaying logout message.");
            model.addAttribute("logoutMessage", "Has cerrado sesión exitosamente.");
        }
        return "login";
    }

    @GetMapping("/")
    public String homePage(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestURI = request.getRequestURI();
        logger.info("Accessing homePage ({}). Authentication object: {}", requestURI, authentication);
        model.addAttribute("requestURI", requestURI); // Pasar requestURI para la lógica de la pestaña activa

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            String username = authentication.getName();
            logger.info("User '{}' is authenticated. Checking roles.", username);

            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority();
                logger.info("User '{}' has authority: {}", username, role);
                if ("ROLE_PACIENTE".equals(role)) {
                    logger.info("User '{}' is ROLE_PACIENTE, redirecting to /imagenes/historial", username);
                    return "redirect:/imagenes/historial";
                } else if ("ROLE_MEDICO".equals(role)) {
                    logger.info("User '{}' is ROLE_MEDICO, forwarding to /medico/dashboard (Inicio - Estadisticas)", username);
                    // Ya no se redirige, se reenvía para que el controlador del dashboard maneje la requestURI
                    return "forward:/medico/dashboard";
                } else if ("ROLE_ADMIN".equals(role)) {
                    logger.info("User '{}' is ROLE_ADMIN, returning 'dashboard-admin' view. Consider forwarding to an admin controller.", username);
                    model.addAttribute("requestURI", "/admin/dashboard"); // Simular URI para admin dashboard si es necesario
                    return "dashboard-admin"; // Considera un controlador específico para admin
                }
            }
            logger.warn("User '{}' authenticated but has an unhandled role. Redirecting to /login.", username);
            return "redirect:/login?error=unauthorized_role";
        } else {
            logger.info("User is not authenticated or is anonymousUser. Redirecting to /login.");
            return "redirect:/login";
        }
    }

    @Controller
    public static class MedicoController {
        private static final Logger medicoLogger = LoggerFactory.getLogger(MedicoController.class);

        @GetMapping("/medico/dashboard")
        public String medicoDashboard(Model model, HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            medicoLogger.info("Accessing Medico Dashboard (Inicio - Contenido de Estadísticas) ({}).", requestURI);
            model.addAttribute("requestURI", requestURI);
            // dashboard-medico.html ahora contiene el layout de estadísticas para la vista de Inicio.
            return "dashboard-medico";
        }

        @GetMapping("/medico/galeria/ver-imagenes")
        public String medicoGaleriaVerImagenes(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            medicoLogger.info("Accessing Medico Galeria Principal ({}).", uri);
            model.addAttribute("requestURI", uri);
            // Nueva plantilla para la galería de imágenes principal.
            return "medico-galeria-principal";
        }

        @GetMapping("/medico/imagenes/cargar-para-paciente")
        public String medicoCargarImagen(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            // Implementa la vista o redirige según sea necesario.
            // Por ejemplo, podría ser un modal en otra página o una página dedicada.
            // Si es un placeholder, redirigir a una vista relevante.
            return "forward:/medico/dashboard"; // O a una página específica de carga de imágenes.
        }

        @GetMapping("/medico/galeria/info-general")
        public String medicoGaleriaInfoGeneral(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            return "forward:/medico/dashboard"; // O a una página de información general de galería.
        }

        @GetMapping("/medico/pacientes/lista")
        public String medicoPacientesLista(Model model, HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            medicoLogger.info("Accessing Medico Pacientes Lista ({}).", requestURI);
            model.addAttribute("requestURI", requestURI);
            return "medico-pacientes-lista";
        }

        @GetMapping("/medico/pacientes/agregar")
        public String medicoAgregarPacienteForm(Model model, HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            medicoLogger.info("Accessing Medico Pacientes Agregar Form ({}). Placeholder.", requestURI);
            model.addAttribute("requestURI", requestURI);
            // Deberías tener una plantilla para esto o redirigir.
            // "redirect:/medico/pacientes/lista" es una opción si no está implementado.
            return "medico-pacientes-lista"; // O una plantilla como "medico-pacientes-agregar"
        }

        @GetMapping("/medico/reportes/generar")
        public String medicoReportesGenerar(Model model, HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            medicoLogger.info("Accessing Medico Reportes Generar ({}).", requestURI);
            model.addAttribute("requestURI", requestURI);
            return "medico-reportes-generar"; // Plantilla para generar reportes
        }

        @GetMapping("/medico/reportes/ver-generados")
        public String medicoReportesVerGenerados(Model model, HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            medicoLogger.info("Accessing Medico Reportes Ver Generados ({}). Placeholder.", requestURI);
            model.addAttribute("requestURI", requestURI);
            // Implementa esta vista. Podría ser similar a 'medico-reportes-generar' pero para visualización.
            return "forward:/medico/dashboard"; // O una plantilla "medico-reportes-ver"
        }


        @GetMapping("/medico/historial/consultas-anteriores")
        public String medicoHistorialConsultas(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            return "forward:/medico/dashboard"; // O una plantilla "medico-historial-consultas"
        }

        // La ruta y el método para /medico/estadisticas han sido eliminados.
    }
}