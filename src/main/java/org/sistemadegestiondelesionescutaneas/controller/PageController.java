package org.sistemadegestiondelesionescutaneas.controller; // Asegúrate que el paquete sea correcto

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

