package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // Importar Lazy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Lazy // Añadir esta anotación
public class Autenticacion {

    private final Usuariorepositorio usuarioRepositorio;
    @Autowired
    private Pacienterepositorio pacienteRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public Autenticacion(Usuariorepositorio usuarioRepositorio, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrousuario(String usuario, String contrasena, String rol, String nombre, String email) {
        if (usuarioRepositorio.findByUsuario(usuario) != null) {
            throw new IllegalArgumentException("Usuario ya existe");
        }

        if (usuarioRepositorio.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        String hashedPassword = passwordEncoder.encode(contrasena);

        Usuario nuevoUsuario = new Usuario(usuario, hashedPassword, rol.toUpperCase(), nombre, email);

        Usuario savedUsuario = usuarioRepositorio.save(nuevoUsuario);

        if ("PACIENTE".equalsIgnoreCase(rol)) {
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setUsuario(savedUsuario);
            nuevoPaciente.setNombre(nombre);
            pacienteRepositorio.save(nuevoPaciente);
        }
        return savedUsuario;
    }

    public Usuario loginUser(String usuario, String contrasena) {
        Usuario usuarioDb = usuarioRepositorio.findByUsuario(usuario);
        if (usuarioDb == null) {
            return null;
        }

        if (passwordEncoder.matches(contrasena, usuarioDb.getContrasena())) {
            return usuarioDb;
        } else {
            return null;
        }
    }
}