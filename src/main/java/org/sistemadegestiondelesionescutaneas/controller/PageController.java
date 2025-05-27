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
    public String homePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Accessing homePage (/). Authentication object: {}", authentication);

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
                    logger.info("User '{}' is ROLE_MEDICO, returning 'dashboard-medico' view", username);
                    return "dashboard-medico"; // This should render dashboard-medico.html
                } else if ("ROLE_ADMIN".equals(role)) {
                    logger.info("User '{}' is ROLE_ADMIN, returning 'dashboard-admin' view", username);
                    return "dashboard-admin";
                }
            }
            // If user has an authenticated role not explicitly handled above
            logger.warn("User '{}' authenticated but has an unhandled role. Redirecting to /login.", username);
            return "redirect:/login?error=unauthorized_role"; // Or a generic error page
        } else {
            logger.info("User is not authenticated or is anonymousUser. Redirecting to /login.");
            return "redirect:/login";
        }
    }

    @Controller
    public class MedicoController {
        @GetMapping("/medico/dashboard")
        public String medicoDashboard(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            // Add any other model attributes needed for dashboard-medico.html
            return "dashboard-medico";
        }

        @GetMapping("/medico/estadisticas")
        public String medicoEstadisticas(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            // Add model attributes for medico-estadisticas.html
            return "medico-estadisticas"; // Assuming you have this template
        }
    }
}