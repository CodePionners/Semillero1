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
        return "login";
    }

    @GetMapping("/")
    public String homePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if ("ROLE_PACIENTE".equals(auth.getAuthority())) {
                    return "redirect:/imagenes/historial"; // MODIFICADO
                } else if ("ROLE_MEDICO".equals(auth.getAuthority())) {
                    return "dashboard-medico";
                } else if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                    // Asumiendo que tienes/tendrás dashboard-admin.html
                    // Si no, redirige a una página apropiada o a /login
                    return "dashboard-admin"; // O, por ejemplo, "redirect:/admin/dashboard" si esa es la ruta
                }
            }
            // Si el rol no coincide con ninguno de los anteriores, redirigir al login
            return "redirect:/login";
        }
        // Si no está autenticado
        return "redirect:/login";
    }

    @GetMapping("/mostrar-registro")
    public String registrationPage() {
        return "registro";
    }
}