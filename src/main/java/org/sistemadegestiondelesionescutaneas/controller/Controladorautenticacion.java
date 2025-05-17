package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.service.Autenticacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class Controladorautenticacion {

    private final Autenticacion autenticacion;

    @Autowired
    public Autenticacion(Autenticacion autenticacion) {
        this.autenticacion = autenticacion;
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody Map<String, String> registrationData) {
        try {
            Usuario nuevousuario = autenticacion.registrousuario(
                    registrationData.get("usuario"),
                    registrationData.get("contraseña"),
                    registrationData.get("rol"),
                    registrationData.get("nombre"),
                    registrationData.get("email")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevousuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Map<String, String> loginData) {
        Usuario usuario1 = autenticacion.loginUser(loginData.get("usuario"), loginData.get("contraseña"));
        if (usuario1 != null) {
            return ResponseEntity.ok(usuario1);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
