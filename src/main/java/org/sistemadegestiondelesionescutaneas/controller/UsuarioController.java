package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.service.Autenticacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError; // Para logging detallado de errores
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private Autenticacion autenticacionService;

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        // logger.info("Mostrando formulario de registro con el objeto usuario: {}", model.getAttribute("usuario")); // toString() debe estar implementado en Usuario
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute("usuario") @Valid Usuario usuarioModel, // Renombrado para claridad
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {

        logger.info("--- INICIO PROCESAR REGISTRO ---");
        if (usuarioModel != null) {
            logger.info("Datos recibidos del formulario en @ModelAttribute usuarioModel:");
            logger.info("Nombre Completo: '{}'", usuarioModel.getNombre());
            logger.info("Nombre de Usuario: '{}'", usuarioModel.getUsuario());
            logger.info("Email: '{}'", usuarioModel.getEmail());
            logger.info("Rol: '{}'", usuarioModel.getRol());
            // No loguear la contraseña directamente, solo su presencia o longitud
            logger.info("Contraseña (presente): '{}'", usuarioModel.getContrasena() != null && !usuarioModel.getContrasena().isEmpty());
        } else {
            logger.error("@ModelAttribute 'usuarioModel' es NULL. Spring no pudo poblar el objeto desde el formulario.");
            redirectAttributes.addFlashAttribute("errorMessage", "Error procesando el formulario. Intente de nuevo.");
            return "redirect:/registro";
        }

        if (result.hasErrors()) {
            logger.warn("Errores de validación encontrados:");
            for (FieldError error : result.getFieldErrors()) {
                logger.warn("Campo: {}, Error: {}, Valor Rechazado: '{}'", error.getField(), error.getDefaultMessage(), error.getRejectedValue());
            }
            if (result.hasGlobalErrors()) {
                result.getGlobalErrors().forEach(error -> logger.warn("Error Global: {}", error.getDefaultMessage()));
            }
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.usuario", result);
            redirectAttributes.addFlashAttribute("usuario", usuarioModel); // Re-poblar el formulario con los datos (y errores)
            return "redirect:/registro";
        }

        try {
            logger.info("No hay errores de validación. Procediendo a llamar al servicio de registro.");
            // Usar los getters del objeto usuarioModel que fue poblado por Spring
            autenticacionService.registrousuario(
                    usuarioModel.getUsuario(),
                    usuarioModel.getContrasena(),
                    usuarioModel.getRol(),
                    usuarioModel.getNombre(),
                    usuarioModel.getEmail()
            );
            redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Por favor, inicia sesión.");
            logger.info("Registro exitoso para el usuario: {}", usuarioModel.getUsuario());
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            logger.error("Error de argumento ilegal durante el registro (desde servicio): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("usuario", usuarioModel);
            return "redirect:/registro";
        } catch (Exception e) {
            logger.error("Error inesperado durante el registro (desde servicio): {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado durante el registro.");
            redirectAttributes.addFlashAttribute("usuario", usuarioModel);
            return "redirect:/registro";
        } finally {
            logger.info("--- FIN PROCESAR REGISTRO ---");
        }
    }
}