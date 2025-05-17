package org.sistemadegestiondelesionescutaneas.model;

public class Usuario {
    private String id;
    private String usuario;
    private String contrasena;
    private String rol; // medico o paciente
    private String nombre;
    private String email;

    public Usuario(String usuario, String contrasena, String rol, String nombre, String email) {
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.nombre = nombre;
        this.email = email;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getNombre() { return nombre; }
    public void setNombre(String name) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}



}
