package org.sistemadegestiondelesionescutaneas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


@Controller
public class PageController {

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
        return "login"; // Devuelve login.html
    }

    @GetMapping("/")
    public String homePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if ("ROLE_PACIENTE".equals(auth.getAuthority())) {
                    // Aquí puedes cargar datos específicos para el dashboard del paciente si es necesario
                    // model.addAttribute("nombreUsuario", authentication.getName());
                    return "dashboard-paciente"; // Nueva plantilla HTML
                } else if ("ROLE_MEDICO".equals(auth.getAuthority())) {
                    // model.addAttribute("nombreUsuario", authentication.getName());
                    return "dashboard-medico"; // Nueva plantilla HTML
                } else if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                    // Podrías tener un dashboard de admin también
                    return "dashboard-admin";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/mostrar-registro")
    public String registrationPage() {
        // Este es el controlador que ya tenías para mostrar la página de registro.
        // El POST para el registro debe ser manejado por otro método,
        // por ejemplo, en UsuarioController como vimos antes.
        return "registro"; // Devuelve registro.html
    }

    @GetMapping("/") // Página principal después de login
    public String homePage() {
        return "dashboard"; // Asume que tienes un dashboard.html
    }
}

