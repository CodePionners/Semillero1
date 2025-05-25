package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email; // Importa Jakarta Bean Validation Email
import jakarta.validation.constraints.NotBlank; // Importa Jakarta Bean Validation NotBlank
import jakarta.validation.constraints.Size;   // Importa Jakarta Bean Validation Size

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @NotBlank(message = "El nombre de usuario no puede estar vacío") // Añadido
    @Size(max = 50, message = "El nombre de usuario no puede exceder los 50 caracteres") // Añadido
    @Column(name = "usuario", unique = true, nullable = false, length = 50)
    private String usuario;

    @NotBlank(message = "La contraseña no puede estar vacía") // Añadido
    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @NotBlank(message = "El rol no puede estar vacío") // Añadido
    @Column(name = "rol", nullable = false, length = 20)
    private String rol;

    @NotBlank(message = "El nombre completo no puede estar vacío") // Añadido
    @Size(max = 100, message = "El nombre completo no puede exceder los 100 caracteres") // Añadido
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email no puede estar vacío") // Añadido
    @Email(message = "Debe ser una dirección de email válida") // Añadido
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres") // Añadido
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Paciente perfilPaciente;


    public Usuario() {}

    public Usuario(String usuario, String contrasena, String rol, String nombre, String email) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Paciente getPerfilPaciente() { return perfilPaciente; }
    public void setPerfilPaciente(Paciente perfilPaciente) { this.perfilPaciente = perfilPaciente; }


    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", usuario='" + usuario + '\'' +
                ", contrasenaPresente=" + (contrasena != null && !contrasena.isEmpty()) +
                ", contrasenaLongitud=" + (contrasena != null ? contrasena.length() : 0) +
                ", rol='" + rol + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}