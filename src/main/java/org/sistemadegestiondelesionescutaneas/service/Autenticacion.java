package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Lazy
public class Autenticacion {

    private static final Logger logger = LoggerFactory.getLogger(Autenticacion.class);

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
    public Usuario registrousuario(String nombreUsuario, String contrasena, String rol, String nombreCompleto, String email) {
        logger.info("Servicio registrousuario: Intentando registrar usuario='{}', rol='{}', nombreCompleto='{}', email='{}'",
                nombreUsuario, rol, nombreCompleto, email);

        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() ||
                contrasena == null || contrasena.trim().isEmpty() || // Contraseña no debería estar vacía
                rol == null || rol.trim().isEmpty() ||
                nombreCompleto == null || nombreCompleto.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            String mensajeError = "Error de validación: Uno o más campos requeridos están vacíos o nulos. " +
                    "nombreUsuario: " + (nombreUsuario == null ? "null" : "'" + nombreUsuario + "'") +
                    ", contrasena presente: " + (contrasena != null && !contrasena.isEmpty()) +
                    ", rol: " + (rol == null ? "null" : "'" + rol + "'") +
                    ", nombreCompleto: " + (nombreCompleto == null ? "null" : "'" + nombreCompleto + "'") +
                    ", email: " + (email == null ? "null" : "'" + email + "'");
            logger.error(mensajeError);
            throw new IllegalArgumentException("Todos los campos son requeridos y no deben estar vacíos.");
        }

        if (usuarioRepositorio.findByUsuario(nombreUsuario) != null) {
            logger.warn("Intento de registrar usuario existente: {}", nombreUsuario);
            throw new IllegalArgumentException("Usuario ya existe");
        }

        if (usuarioRepositorio.findByEmail(email) != null) {
            logger.warn("Intento de registrar email existente: {}", email);
            throw new IllegalArgumentException("Email ya registrado");
        }

        String hashedPassword = passwordEncoder.encode(contrasena);
        logger.debug("Contraseña hasheada para {}: (longitud {})", nombreUsuario, hashedPassword.length());

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario(nombreUsuario);
        nuevoUsuario.setContrasena(hashedPassword);
        nuevoUsuario.setRol(rol.toUpperCase());
        nuevoUsuario.setNombre(nombreCompleto);
        nuevoUsuario.setEmail(email);

        // Logging CRÍTICO: Verifica el estado del objeto ANTES de guardar
        logger.info("Objeto Usuario ANTES de guardar: {}", nuevoUsuario.toString()); // Asegúrate que Usuario.toString() no muestre la contraseña

        Usuario savedUsuario;
        try {
            savedUsuario = usuarioRepositorio.save(nuevoUsuario);
            logger.info("Usuario guardado con ID: {}. Nombre de usuario: {}, Email: {}, Rol: {}",
                    savedUsuario.getId(), savedUsuario.getUsuario(), savedUsuario.getEmail(), savedUsuario.getRol());
            // Logging CRÍTICO: Verifica el estado del objeto DESPUÉS de guardar (obtenido de la BD)
            // Esto es si 'save' devuelve la entidad persistida con el ID generado.
            // Si la entidad Usuario tiene un toString() bien definido, esto es útil.
            logger.info("Objeto Usuario DESPUÉS de guardar (devuelto por save): {}", savedUsuario.toString());


        } catch (Exception e) {
            logger.error("EXCEPCIÓN al intentar guardar el usuario: {}", nuevoUsuario.getUsuario(), e);
            // Relanzar para que el controlador la maneje o manejarla aquí si es apropiado
            throw new RuntimeException("Error al guardar el usuario en la base de datos.", e);
        }

        if ("PACIENTE".equalsIgnoreCase(rol)) {
            logger.info("Creando perfil de Paciente para usuario: {}", savedUsuario.getUsuario());
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setUsuario(savedUsuario);
            nuevoPaciente.setNombre(nombreCompleto);
            try {
                pacienteRepositorio.save(nuevoPaciente);
                logger.info("Perfil de Paciente creado para usuario ID: {}", savedUsuario.getId());
            } catch (Exception e) {
                logger.error("EXCEPCIÓN al intentar guardar el perfil del paciente para el usuario: {}", savedUsuario.getUsuario(), e);
                // Considera cómo manejar este error (¿rollback de la creación del usuario?)
                throw new RuntimeException("Error al guardar el perfil del paciente.", e);
            }
        }
        return savedUsuario;
    } // Cierre del método registrousuario

    // El método loginUser no es utilizado por Spring Security para el proceso de login del formulario.
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