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
        model.addAttribute("requestURI", requestURI);

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
                    logger.info("User '{}' is ROLE_MEDICO, forwarding to /medico/dashboard", username);
                    return "forward:/medico/dashboard";
                } else if ("ROLE_ADMIN".equals(role)) {
                    logger.info("User '{}' is ROLE_ADMIN, returning 'dashboard-admin' view. Consider forwarding to an admin controller.", username);
                    return "dashboard-admin";
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
            medicoLogger.info("Accessing Medico Dashboard ({}).", requestURI);
            model.addAttribute("requestURI", requestURI);
            return "dashboard-medico";
        }

        @GetMapping("/medico/estadisticas")
        public String medicoEstadisticas(Model model, HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            medicoLogger.info("Accessing Medico Estadisticas ({}).", requestURI);
            model.addAttribute("requestURI", requestURI);
            return "medico-estadisticas";
        }

        @GetMapping("/medico/pacientes/lista")
        public String medicoPacientesLista(Model model, HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            medicoLogger.info("Accessing Medico Pacientes Lista ({}). ESTE LOG ES CLAVE.", requestURI);
            model.addAttribute("requestURI", requestURI);
            return "medico-pacientes-lista";
        }

        @GetMapping("/medico/pacientes/agregar")
        public String medicoAgregarPacienteForm(Model model, HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            medicoLogger.info("Accessing Medico Pacientes Agregar Form ({}). Placeholder.", requestURI);
            model.addAttribute("requestURI", requestURI);
            medicoLogger.warn("Placeholder for /medico/pacientes/agregar. Implement actual form or view. Redirecting to lista for now.");
            return "redirect:/medico/pacientes/lista";
        }

        @GetMapping("/medico/galeria/ver-imagenes")
        public String medicoGaleriaVerImagenes(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            return "forward:/medico/dashboard";
        }

        @GetMapping("/medico/imagenes/cargar-para-paciente")
        public String medicoCargarImagen(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            return "forward:/medico/dashboard";
        }

        @GetMapping("/medico/galeria/info-general")
        public String medicoGaleriaInfoGeneral(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            return "forward:/medico/dashboard";
        }

        @GetMapping("/medico/reportes/generar")
        public String medicoReportesGenerar(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            return "forward:/medico/dashboard";
        }

        @GetMapping("/medico/reportes/ver-generados")
        public String medicoReportesVer(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            return "forward:/medico/dashboard";
        }

        @GetMapping("/medico/historial/consultas-anteriores")
        public String medicoHistorialConsultas(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            model.addAttribute("requestURI", uri);
            medicoLogger.info("Accessing {}. Placeholder - implement view or redirect.", uri);
            return "forward:/medico/dashboard";


        }
    }

    @Controller
    public static class MedicoController {
        private static final Logger medicoLogger = LoggerFactory.getLogger(MedicoController.class);


        @GetMapping("/medico/reportes/generar") // Este es el enlace del submenú
        public String medicoReportesGenerar(Model model, HttpServletRequest request) {
            String uri = request.getRequestURI();
            medicoLogger.info("Accessing Medico Reportes Generar ({}).", uri); // Log para depuración
            model.addAttribute("requestURI", uri);
            return "medico-reportes-generar"; // Nombre del nuevo archivo HTML


            @GetMapping("/medico/reportes/ver-generados")
            public String medicoReportesVer (Model model, HttpServletRequest request){
                String uri = request.getRequestURI();
                medicoLogger.info("Accessing Medico Reportes Ver ({}). Placeholder.", uri);
                model.addAttribute("requestURI", uri);
                // Cuando implementes esta vista, cambia el return:
                // return "medico-reportes-lista";
                return "forward:/medico/reportes/generar"; // Temporalmente reenvía o implementa la vista
            }
        }
    }
}
