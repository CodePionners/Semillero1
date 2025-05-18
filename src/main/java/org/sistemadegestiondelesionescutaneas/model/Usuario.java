package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*; // O javax.persistence.*
// import java.util.List; // Si decides añadir relaciones inversas

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id; // autoincremental

    @Column(unique = true, nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false, length = 20)
    private String rol; // "medico" o "paciente"

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    // Relación opcional con Paciente (si un Usuario tiene un perfil de Paciente)

    public Usuario() {
    }

    public Usuario(String usuario, String contrasena, String rol, String nombre, String email) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y Setters para todos los campos, incluyendo el nuevo 'id'
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    } // Corregido el parámetro de setNombre

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}