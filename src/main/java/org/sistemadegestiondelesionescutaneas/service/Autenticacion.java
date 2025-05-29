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
    private final Pacienterepositorio pacienteRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public Autenticacion(Usuariorepositorio usuarioRepositorio,
                         Pacienterepositorio pacienteRepositorio,
                         BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrousuario(String nombreUsuario, String contrasena, String rol, String nombreCompleto, String email, String identificacionPacienteForm) {
        logger.info("Servicio registrousuario: Intentando registrar usuario='{}', rol='{}', nombreCompleto='{}', email='{}', identificacionPacienteForm='{}'",
                nombreUsuario, rol, nombreCompleto, email, identificacionPacienteForm);

        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() ||
                contrasena == null || contrasena.trim().isEmpty() ||
                rol == null || rol.trim().isEmpty() ||
                nombreCompleto == null || nombreCompleto.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            String mensajeError = "Error de validación: Uno o más campos básicos requeridos están vacíos o nulos.";
            logger.error(mensajeError);
            throw new IllegalArgumentException("Todos los campos básicos (nombre, usuario, email, contraseña, rol) son requeridos.");
        }

        if (usuarioRepositorio.findByUsuario(nombreUsuario) != null) {
            logger.warn("Intento de registrar usuario existente: {}", nombreUsuario);
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }

        if (usuarioRepositorio.findByEmail(email) != null) {
            logger.warn("Intento de registrar email existente: {}", email);
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        // Validación específica para rol PACIENTE
        if ("PACIENTE".equalsIgnoreCase(rol)) {
            if (identificacionPacienteForm == null || identificacionPacienteForm.trim().isEmpty()) {
                logger.error("Error de validación: La identificación es requerida para el rol PACIENTE.");
                throw new IllegalArgumentException("La identificación del paciente es requerida para el rol PACIENTE.");
            }
            // Comprueba si ya existe un Paciente con esta identificación
            if (pacienteRepositorio.findByIdentificacion(identificacionPacienteForm).isPresent()) { //
                logger.warn("Intento de registrar paciente con identificación existente: {}", identificacionPacienteForm);
                throw new IllegalArgumentException("La identificación del paciente ya está registrada.");
            }
        }

        String hashedPassword = passwordEncoder.encode(contrasena);
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario(nombreUsuario);
        nuevoUsuario.setContrasena(hashedPassword);
        nuevoUsuario.setRol(rol.toUpperCase());
        nuevoUsuario.setNombre(nombreCompleto);
        nuevoUsuario.setEmail(email);
        // Nota: identificacionPacienteForm del modelo Usuario es transitorio
        // Se usará para establecer la identificación en la entidad Paciente.

        Usuario savedUsuario;
        try {
            savedUsuario = usuarioRepositorio.save(nuevoUsuario);
            logger.info("Usuario guardado con ID: {}. Nombre de usuario: {}", savedUsuario.getId(), savedUsuario.getUsuario());
        } catch (Exception e) {
            logger.error("EXCEPCIÓN al intentar guardar el usuario: {}", nuevoUsuario.getUsuario(), e);
            throw new RuntimeException("Error al guardar el usuario en la base de datos.", e);
        }

        if ("PACIENTE".equalsIgnoreCase(rol)) {
            logger.info("Creando perfil de Paciente para usuario: {} con identificación: {}", savedUsuario.getUsuario(), identificacionPacienteForm);
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setUsuario(savedUsuario);
            nuevoPaciente.setNombre(nombreCompleto);
            nuevoPaciente.setIdentificacion(identificacionPacienteForm); // Establece la identificación para el Paciente

            try {
                Paciente savedPaciente = pacienteRepositorio.save(nuevoPaciente);
                logger.info("Perfil de Paciente creado con ID: {} para usuario ID: {}", savedPaciente.getId(), savedUsuario.getId());
                savedUsuario.setPerfilPaciente(savedPaciente);
                // Volver a guardar el usuario si el perfil del Paciente se estableció después del guardado inicial y necesita persistirse en el lado del Usuario de OneToOne si es propietario del mapeo de la relación
                // Sin embargo, @OneToOne(mappedBy = "usuario") en Usuario.perfilPaciente significa que Paciente es propietario de la FK.
                // Por lo tanto, guardar Paciente con el usuario vinculado es suficiente.
            } catch (Exception e) {
                logger.error("EXCEPCIÓN al intentar guardar el perfil del paciente para el usuario: {}. Identificación: {}",
                        savedUsuario.getUsuario(), identificacionPacienteForm, e);
                // Considera cómo manejar esto transaccionalmente. Si falla el guardado de Paciente, Usuario idealmente debería revertirse.
                // @Transactional en el método debería encargarse de esto.
                throw new RuntimeException("Error al guardar el perfil del paciente. El registro de usuario será revertido.", e);
            }
        }
        return savedUsuario;
    }

    public Usuario loginUser(String usuario, String contrasena) {
        Usuario usuarioDb = usuarioRepositorio.findByUsuario(usuario);
        if (usuarioDb == null) {
            logger.warn("Intento de inicio de sesión para usuario no existente: {}", usuario);
            return null;
        }
        if (passwordEncoder.matches(contrasena, usuarioDb.getContrasena())) {
            logger.info("Inicio de sesión exitoso para el usuario: {}", usuario);
            return usuarioDb;
        } else {
            logger.warn("Inicio de sesión fallido (contraseña incorrecta) para el usuario: {}", usuario);
            return null;
        }
    }
}