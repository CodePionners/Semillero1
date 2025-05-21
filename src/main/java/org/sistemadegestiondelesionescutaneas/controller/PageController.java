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

    @GetMapping("/") // Este es el mapeo principal de la página de inicio
    public String homePage(Model model) { // Se añadió Model de nuevo, puede ser útil
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Comprobar si el usuario está autenticado y no es el usuario anónimo
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
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
                    return "dashboard-admin"; // Asumiendo que tienes/tendrás dashboard-admin.html
                }
            }
            // Si el usuario está autenticado pero no tiene un rol coincidente,
            // o si anonymousUser de alguna manera llega aquí y no es capturado por permitAll en SecurityConfig.
            return "redirect:/login"; // O una página de error genérica, o acceso denegado
        }
        // Si no está autenticado (p. ej., usuario anónimo accediendo a / directamente antes de que la seguridad actúe para /login)
        return "redirect:/login";
    }

    @GetMapping("/mostrar-registro")
    public String registrationPage() {
        // Este es el controlador que ya tenías para mostrar la página de registro.
        // El POST para el registro debe ser manejado por otro método,
        // por ejemplo, en UsuarioController como vimos antes.
        return "registro"; // Devuelve registro.html
    }
}