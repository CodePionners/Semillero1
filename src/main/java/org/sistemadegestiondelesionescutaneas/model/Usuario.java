package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
// ... (otras importaciones para caché si las tienes)

@Entity
@Table(name = "usuarios")
// ... (anotaciones de caché si las tienes) ...
public class Usuario {

    // ... (tus campos y constructor) ...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "usuario", unique = true, nullable = false, length = 50)
    private String usuario;

    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

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

    // ... (tus getters y setters) ...
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
                // No mostrar la contraseña hasheada en logs por seguridad,
                // pero sí confirmar si está presente y su longitud.
                ", contrasenaPresente=" + (contrasena != null && !contrasena.isEmpty()) +
                ", contrasenaLongitud=" + (contrasena != null ? contrasena.length() : 0) +
                ", rol='" + rol + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}