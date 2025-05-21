package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Autenticacion {

    private final Usuariorepositorio usuarioRepositorio;
    @Autowired
    private Pacienterepositorio pacienteRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public Autenticacion(Usuariorepositorio usuarioRepositorio, BCryptPasswordEncoder passwordEncoder) { // Añadir BCryptPasswordEncoder como parámetro
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional // Recomendado para atomicidad
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
        Usuario nuevoUsuario = new Usuario(usuario, hashedPassword, rol.toUpperCase(), nombre, email); // Almacenar rol consistentemente (ej. mayúsculas)

        Usuario savedUsuario = usuarioRepositorio.save(nuevoUsuario); // Guardar el usuario primero

        if ("PACIENTE".equalsIgnoreCase(rol)) {
            Paciente nuevoPaciente = new Paciente();
            nuevoPaciente.setUsuario(savedUsuario); // Vincula con el Usuario recién creado
            nuevoPaciente.setNombre(nombre); // Puedes usar el nombre proporcionado en el registro
            // Quizás quieras establecer otros campos predeterminados de Paciente aquí
            pacienteRepositorio.save(nuevoPaciente);
            // Opcional: si tienes una relación bidireccional y quieres establecer Paciente en Usuario
            // savedUsuario.setPerfilPaciente(nuevoPaciente); // Asegúrate que esto no cause problemas con entidades desconectadas si no es manejado correctamente por JPA
            // usuarioRepositorio.save(savedUsuario); // Si modificas savedUsuario después del guardado inicial
        }
        return savedUsuario; // Retornar el usuario guardado
    }

    public Usuario loginUser(String usuario, String contrasena) {
        Usuario usuarioDb = usuarioRepositorio.findByUsuario(usuario); // Variable renombrada para claridad
        if (usuarioDb == null) {
            return null; // Usuario no encontrado
        }

        if (passwordEncoder.matches(contrasena, usuarioDb.getContrasena())) { // Verificar la contraseña
            return usuarioDb;
        } else {
            return null;
        }
    }
}