package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.service.Autenticacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError; // Para errores globales y de campo
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
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("usuario") Usuario usuarioModel,
                                   BindingResult result,
                                   Model model, // Añadido para devolver a la misma página con errores
                                   RedirectAttributes redirectAttributes) {

        logger.info("--- INICIO PROCESAR REGISTRO ---");
        if (usuarioModel != null) {
            logger.info("Datos recibidos del formulario: Nombre Completo: '{}', Usuario: '{}', Email: '{}', Rol: '{}', Contraseña (presente): '{}'",
                    usuarioModel.getNombre(),
                    usuarioModel.getUsuario(), // Nombre del campo en el formulario
                    usuarioModel.getEmail(),
                    usuarioModel.getRol(),
                    (usuarioModel.getContrasena() != null && !usuarioModel.getContrasena().isEmpty()));
        }

        if (result.hasErrors()) {
            logger.warn("Errores de validación y/o binding encontrados en BindingResult:");
            for (ObjectError error : result.getAllErrors()) {
                if (error instanceof FieldError) {
                    FieldError fieldError = (FieldError) error;
                    logger.warn("Error de Campo: Objeto='{}', Campo='{}', Valor Rechazado='{}', Código='{}', Mensaje='{}'",
                            fieldError.getObjectName(),
                            fieldError.getField(),
                            fieldError.getRejectedValue(),
                            fieldError.getCode(),
                            error.getDefaultMessage());
                } else {
                    logger.warn("Error Global: Objeto='{}', Código='{}', Mensaje='{}'",
                            error.getObjectName(),
                            error.getCode(),
                            error.getDefaultMessage());
                }
            }
            model.addAttribute("usuario", usuarioModel); // Devolver el objeto con los datos ingresados
            // model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "usuario", result); // Opcional, Thymeleaf suele encontrarlo
            return "registro"; // Volver a la página de registro para mostrar errores
        }

        try {
            logger.info("No hay errores en BindingResult. Procediendo a llamar al servicio de registro.");
            autenticacionService.registrousuario(
                    usuarioModel.getUsuario(),
                    usuarioModel.getContrasena(),
                    usuarioModel.getRol(),
                    usuarioModel.getNombre(),
                    usuarioModel.getEmail()
            );
            redirectAttributes.addFlashAttribute("successMessage", "¡Registro exitoso! Por favor, inicia sesión.");
            logger.info("Registro supuestamente exitoso para el usuario: {}", usuarioModel.getUsuario());
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            logger.error("Error de argumento ilegal durante el registro (desde servicio): {}", e.getMessage(), e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("usuario", usuarioModel);
            return "registro";
        } catch (Exception e) {
            logger.error("Error inesperado durante el registro (desde servicio): {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Ocurrió un error inesperado durante el registro.");
            model.addAttribute("usuario", usuarioModel);
            return "registro";
        } finally {
            logger.info("--- FIN PROCESAR REGISTRO ---");
        }
    }
}