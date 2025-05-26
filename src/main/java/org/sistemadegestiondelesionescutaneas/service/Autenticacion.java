package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // Conservado según el código original
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Lazy // Conservado según el código original
public class Autenticacion {

    private static final Logger logger = LoggerFactory.getLogger(Autenticacion.class);

    private final Usuariorepositorio usuarioRepositorio;
    private final Pacienterepositorio pacienteRepositorio; // Hecho final
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public Autenticacion(Usuariorepositorio usuarioRepositorio,
                         Pacienterepositorio pacienteRepositorio, // Añadido al constructor
                         BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.pacienteRepositorio = pacienteRepositorio; // Inicializar aquí
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrousuario(String nombreUsuario, String contrasena, String rol, String nombreCompleto, String email) {
        logger.info("Servicio registrousuario: Intentando registrar usuario='{}', rol='{}', nombreCompleto='{}', email='{}'",
                nombreUsuario, rol, nombreCompleto, email);

        // Validación de campos vacíos (ya presente y correcta)
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() ||
                contrasena == null || contrasena.trim().isEmpty() ||
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

        // Verificación de usuario/email existente (ya presente y correcta)
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
        nuevoUsuario.setRol(rol.toUpperCase()); // Asegurar mayúsculas/minúsculas consistentes para el rol
        nuevoUsuario.setNombre(nombreCompleto);
        nuevoUsuario.setEmail(email);

        // Logging CRÍTICO: Verifica el estado del objeto ANTES de guardar
        logger.info("Objeto Usuario ANTES de guardar: {}", nuevoUsuario.toString());

        Usuario savedUsuario;
        try {
            savedUsuario = usuarioRepositorio.save(nuevoUsuario);
            logger.info("Usuario guardado con ID: {}. Nombre de usuario: {}, Email: {}, Rol: {}",
                    savedUsuario.getId(), savedUsuario.getUsuario(), savedUsuario.getEmail(), savedUsuario.getRol());
            // Logging CRÍTICO: Verifica el estado del objeto DESPUÉS de guardar
            logger.info("Objeto Usuario DESPUÉS de guardar (devuelto por save): {}", savedUsuario.toString());

        } catch (Exception e) {
            logger.error("EXCEPCIÓN al intentar guardar el usuario: {}", nuevoUsuario.getUsuario(), e);
            // Esta excepción causará que el método @Transactional haga rollback.
            throw new RuntimeException("Error al guardar el usuario en la base de datos.", e);
        }

        if ("PACIENTE".equalsIgnoreCase(rol)) { // Usar equalsIgnoreCase por robustez
            logger.info("Creando perfil de Paciente para usuario: {}", savedUsuario.getUsuario());
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setUsuario(savedUsuario); // Enlaza Paciente al Usuario guardado (y gestionado)
            nuevoPaciente.setNombre(nombreCompleto); // Establece el nombre del Paciente

            try {
                Paciente savedPaciente = pacienteRepositorio.save(nuevoPaciente);
                logger.info("Perfil de Paciente creado con ID: {} para usuario ID: {}", savedPaciente.getId(), savedUsuario.getId());

                // Para consistencia del grafo de objetos dentro de esta transacción/sesión:
                // Establece el perfil Paciente en el objeto Usuario.
                // Dado que Usuario.perfilPaciente tiene mappedBy, esto no causará una actualización extra en la BD
                // para la columna FK en sí, pero asegura que el grafo de objetos Java sea consistente.
                savedUsuario.setPerfilPaciente(savedPaciente);

            } catch (Exception e) {
                // Loguear el estado del objeto Paciente si es posible (requiere Paciente.toString())
                logger.error("EXCEPCIÓN al intentar guardar el perfil del paciente para el usuario: {}. Detalles del Paciente: {}",
                        savedUsuario.getUsuario(), nuevoPaciente.toString(), e);
                // Esta excepción causará que el método @Transactional haga rollback,
                // por lo que 'savedUsuario' tampoco se persistirá en la BD.
                throw new RuntimeException("Error al guardar el perfil del paciente. El registro de usuario será revertido.", e);
            }
        }
        return savedUsuario;
    }

    // Método loginUser (ya presente y parece correcto para un login directo sin Spring Security)
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