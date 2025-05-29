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
    public Usuario registrousuario(String nombreUsuario, String contrasena, String rol, String nombreCompleto, String email, String identificacionPaciente) {
        logger.info("Servicio registrousuario: Intentando registrar usuario='{}', rol='{}', nombreCompleto='{}', email='{}', identificacionPaciente='{}'",
                nombreUsuario, rol, nombreCompleto, email, identificacionPaciente);

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
            if (identificacionPaciente == null || identificacionPaciente.trim().isEmpty()) {
                logger.error("Error de validación: La identificación es requerida para el rol PACIENTE.");
                throw new IllegalArgumentException("La identificación del paciente es requerida para el rol PACIENTE.");
            }
            // Utilizar el método findByIdentificacion que devuelve Optional<Paciente>
            if (pacienteRepositorio.findByIdentificacion(identificacionPaciente).isPresent()) { //
                logger.warn("Intento de registrar paciente con identificación existente: {}", identificacionPaciente);
                throw new IllegalArgumentException("La identificación del paciente ya está registrada.");
            }
        }

        String hashedPassword = passwordEncoder.encode(contrasena);
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario(nombreUsuario);
        nuevoUsuario.setContrasena(hashedPassword);
        nuevoUsuario.setRol(rol.toUpperCase()); // Asegurar que el rol se guarde en mayúsculas para consistencia con Spring Security
        nuevoUsuario.setNombre(nombreCompleto);
        nuevoUsuario.setEmail(email);

        Usuario savedUsuario;
        try {
            savedUsuario = usuarioRepositorio.save(nuevoUsuario);
            logger.info("Usuario guardado con ID: {}. Nombre de usuario: {}", savedUsuario.getId(), savedUsuario.getUsuario());
        } catch (Exception e) {
            logger.error("EXCEPCIÓN al intentar guardar el usuario: {}", nuevoUsuario.getUsuario(), e);
            throw new RuntimeException("Error al guardar el usuario en la base de datos.", e);
        }

        if ("PACIENTE".equalsIgnoreCase(rol)) { //
            logger.info("Creando perfil de Paciente para usuario: {} con identificación: {}", savedUsuario.getUsuario(), identificacionPaciente);
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setUsuario(savedUsuario); // Asociar el usuario al paciente
            nuevoPaciente.setNombre(nombreCompleto); // El nombre del paciente es el nombre completo del usuario
            nuevoPaciente.setIdentificacion(identificacionPaciente); // Establecer la identificación

            // Aquí podrías establecer otros campos por defecto para Paciente si es necesario
            // nuevoPaciente.setEdad(...);
            // nuevoPaciente.setSexo(...);

            try {
                Paciente savedPaciente = pacienteRepositorio.save(nuevoPaciente);
                logger.info("Perfil de Paciente creado con ID: {} para usuario ID: {}", savedPaciente.getId(), savedUsuario.getId());
                savedUsuario.setPerfilPaciente(savedPaciente); // Actualizar la referencia en el objeto Usuario
                // No es necesario un save explícito de savedUsuario aquí si la transacción sigue activa y @OneToOne tiene el cascade adecuado
                // o si la relación es gestionada correctamente por JPA. Hibernate marcará savedUsuario como dirty y lo actualizará.
            } catch (Exception e) {
                logger.error("EXCEPCIÓN al intentar guardar el perfil del paciente para el usuario: {}. Identificación: {}",
                        savedUsuario.getUsuario(), identificacionPaciente, e);
                // Considerar si se debe eliminar el 'savedUsuario' si la creación del paciente falla,
                // o manejar la transacción para que haga rollback completo.
                // @Transactional se encargará del rollback si se lanza una RuntimeException.
                throw new RuntimeException("Error al guardar el perfil del paciente. El registro de usuario será revertido.", e);
            }
        }
        return savedUsuario;
    }

    public Usuario loginUser(String usuario, String contrasena) {
        Usuario usuarioDb = usuarioRepositorio.findByUsuario(usuario);
        if (usuarioDb == null) {
            logger.warn("Intento de login para usuario no existente: {}", usuario);
            return null;
        }
        if (passwordEncoder.matches(contrasena, usuarioDb.getContrasena())) {
            logger.info("Login exitoso para usuario: {}", usuario);
            return usuarioDb;
        } else {
            logger.warn("Intento de login fallido (contraseña incorrecta) para usuario: {}", usuario);
            return null;
        }
    }
}