package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Autenticacion {

    private final Usuariorepositorio usuarioRepositorio;

    @Autowired
    public Autenticacion (Usuariorepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Usuario registrousuario(String usuario, String contrasena, String rol, String nombre, String email) {
        // Validar si el usuario ya existe (usuario o email)
        if (usuarioRepositorio.findByUsuario(usuario) != null) {
            throw new IllegalArgumentException("Usuario ya existe");
        }

        //    Hashear las contraseñas en texto plano!
        String hashedPassword = hashPassword(contrasena); // Implementar hashPassword()

        // 3. Crear el nuevo usuario
        Usuario nuevousuario = new Usuario(usuario, hashedPassword, rol, nombre, email);

        // 4. Guardar el usuario en la base de datos
        return usuarioRepositorio.save(nuevousuario);
    }

    public Usuario loginUser(String usuario, String contrasena) {
        Usuario usuario1 = usuarioRepositorio.findByUsuario(usuario);
        if (usuario1 == null) {
            return null; // Usuario no encontrado
        }

        // Verificar la contraseña hasheada
        if (verificarcontrasena(contrasena, usuario1.getContrasena())) { // Implementar verifyPassword()
            return usuario1;
        } else {
            return null; // Contraseña incorrecta
        }
    }

    private String hashPassword(String contrasena) {
        // Implementar un algoritmo de hash seguro (BCrypt, Argon2)
        // ¡NO uses MD5 o SHA-1 para contraseñas!
        return contrasena; // Reemplazar con la implementación real
    }

    private boolean verificarcontrasena(String contrasena, String hashedPassword) {
        // Implementar la verificación de la contraseña hasheada
        return contrasena.equals(hashedPassword); // Reemplazar con la implementación real
    }
}