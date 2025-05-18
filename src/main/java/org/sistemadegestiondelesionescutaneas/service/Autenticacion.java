package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class Autenticacion {

    private final Usuariorepositorio usuarioRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;


    @Autowired
    public Autenticacion(Usuariorepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrousuario(String usuario, String contrasena, String rol, String nombre, String email) {
        // Validar si el usuario ya existe (usuario o email)
        if (usuarioRepositorio.findByUsuario(usuario) != null) {
            throw new IllegalArgumentException("Usuario ya existe");
        }

        if (usuarioRepositorio.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email ya registrado");
        }
            //    Hashear las contraseñas en texto plano!
        String hashedPassword = passwordEncoder.encode(contrasena);

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

        if (passwordEncoder.matches(contrasena, usuario1.getContrasena())) { // Verificar la contraseña
            return usuario1;
        } else {
            return null;
        }
    }
}