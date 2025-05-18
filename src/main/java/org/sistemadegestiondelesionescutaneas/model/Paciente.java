package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long id; // Cambiado de String a Long

    @Column(nullable = false, length = 100)
    private String nombre;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;

    // Relación opcional con Usuario (si un Paciente está vinculado a una cuenta de Usuario)

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImagenLesion> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalisisDermatologico> analisis = new ArrayList<>();

    // Asumiendo que Historiaclinica es EntradaHistorial y tienes una entidad EntradaHistorial
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntradaHistorial> historial = new ArrayList<>(); // Cambiado a EntradaHistorial

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reporte> reportes = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HistorialLesionPrevia> historialLesionesPrevias = new ArrayList<>();


    public Paciente() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public List<ImagenLesion> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenLesion> imagenes) { this.imagenes = imagenes; }
    public List<AnalisisDermatologico> getAnalisis() { return analisis; }
    public void setAnalisis(List<AnalisisDermatologico> analisis) { this.analisis = analisis; }
    public List<EntradaHistorial> getHistorial() { return historial; }
    public void setHistorial(List<EntradaHistorial> historial) { this.historial = historial; }
    public List<Reporte> getReportes() { return reportes; }
    public void setReportes(List<Reporte> reportes) { this.reportes = reportes; }
    public List<HistorialLesionPrevia> getHistorialLesionesPrevias() { return historialLesionesPrevias; }
    public void setHistorialLesionesPrevias(List<HistorialLesionPrevia> historialLesionesPrevias) { this.historialLesionesPrevias = historialLesionesPrevias; }

}