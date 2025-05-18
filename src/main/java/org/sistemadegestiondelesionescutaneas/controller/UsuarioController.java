package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.model.Usuario; // Tu entidad
import org.sistemadegestiondelesionescutaneas.service.Autenticacion; // Tu servicio de registro
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Para validación
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;

@Controller
public class UsuarioController {

    @Autowired
    private Autenticacion autenticacionService;

    // Muestra el formulario de registro
    @GetMapping("/registro") // Este GET es para mostrar/recargar el formulario
    public String mostrarFormularioRegistro(Model model) {
        if (!model.containsAttribute("usuario")) { // Evita sobrescribir si viene de una redirección con error
            model.addAttribute("usuario", new Usuario());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute("usuario") /*@Valid*/ Usuario usuario,
                                   BindingResult result, // Para errores de validación
                                   RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) { // Si usas Bean Validation
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.usuario", result);
        redirectAttributes.addFlashAttribute("usuario", usuario);
        return "redirect:/registro";
        }

        try {
            autenticacionService.registrousuario(
                    usuario.getUsuario(),
                    usuario.getContrasena(), // La contraseña se hasheará en el servicio
                    usuario.getRol(),
                    usuario.getNombre(),
                    usuario.getEmail()
            );
            redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Por favor, inicia sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            // Este error es si el usuario ya existe
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("usuario", usuario); // Para rellenar el formulario
            return "redirect:/registro"; // Vuelve al formulario
        } catch (Exception e) {
            // Otros errores
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado durante el registro: " + e.getMessage());
            redirectAttributes.addFlashAttribute("usuario", usuario);
            return "redirect:/registro";
        }
    }
}