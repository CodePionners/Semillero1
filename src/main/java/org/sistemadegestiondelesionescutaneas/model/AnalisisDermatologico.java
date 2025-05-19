package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "analisis_dermatologicos")
public class AnalisisDermatologico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_analisis")
    private Long id; // Cambiado de String a Long

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Cada análisis pertenece a un paciente
    @JoinColumn(name = "id_paciente", nullable = false)  // FK
    private Paciente paciente;

    @OneToOne(fetch = FetchType.LAZY, optional = false) // Cada análisis es sobre una imagen específica
    @JoinColumn(name = "id_imagen_lesion", nullable = false, unique = true) // FK y Única
    private ImagenLesion imagen;

    @Column(nullable = false)
    private LocalDateTime fechahoraanalisis;

    // Atributos Clínicos (considera si algunos deberían ser Enums)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo; // Asumiendo Enum Sexo

    @Enumerated(EnumType.STRING) // Si EdadEstimada es un Enum
    @Column(length = 50)
    private EdadEstimada edadestimada; // Asumiendo Enum EdadEstimada

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private AreaCorporalAfectada areacorporalafectada; // Asumiendo Enum

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private TipoPielFitzpatrick tipopielfitzpatrick; // Asumiendo Enum

    @Enumerated(EnumType.STRING) // Si Tamanodelesion es un Enum
    @Column(length = 100)
    private TamanodeLesion tamanodelesion;


    @Enumerated(EnumType.STRING) // Si AntecedentesFamiliaresCancer es un Enum
    @Column(length = 100)
    private AntecedentesFamiliaresCancer antecedentesfamiliarescancer;

    private Boolean historiallesionesprevias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Diagnostico diagnostico;

    @OneToMany(mappedBy = "analisisreferencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntradaHistorial> entradasHistorial;

    @OneToMany(mappedBy = "analisisreferencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reporte> reportesGenerados;

    public AnalisisDermatologico() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public ImagenLesion getImagen() { return imagen; }
    public void setImagen(ImagenLesion imagen) { this.imagen = imagen; }
    public LocalDateTime getFechahoraanalisis() { return fechahoraanalisis; }
    public void setFechahoraanalisis(LocalDateTime fechahoraanalisis) { this.fechahoraanalisis = fechahoraanalisis; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public EdadEstimada getEdadestimada() { return edadestimada; }
    public void setEdadestimada(EdadEstimada edadestimada) { this.edadestimada = edadestimada; }
    public AreaCorporalAfectada getAreacorporalafectada() { return areacorporalafectada; }
    public void setAreacorporalafectada(AreaCorporalAfectada areacorporalafectada) { this.areacorporalafectada = areacorporalafectada; }
    public TipoPielFitzpatrick getTipopielfitzpatrick() { return tipopielfitzpatrick; }
    public void setTipopielfitzpatrick(TipoPielFitzpatrick tipopielfitzpatrick) { this.tipopielfitzpatrick = tipopielfitzpatrick; }
    public TamanodeLesion getTamanodelesion() { return tamanodelesion; }
    public void setTamanodelesion(TamanodeLesion tamanodelesion) { this.tamanodelesion = tamanodelesion; }
    public AntecedentesFamiliaresCancer getAntecedentesfamiliarescancer() { return antecedentesfamiliarescancer; }
    public void setAntecedentesfamiliarescancer(AntecedentesFamiliaresCancer antecedentesfamiliarescancer) { this.antecedentesfamiliarescancer = antecedentesfamiliarescancer; }
    public Boolean getHistoriallesionesprevias() { return historiallesionesprevias; }
    public void setHistoriallesionesprevias(Boolean historiallesionesprevias) { this.historiallesionesprevias = historiallesionesprevias; }
    public Diagnostico getDiagnostico() { return diagnostico; }
    public void setDiagnostico(Diagnostico diagnostico) { this.diagnostico = diagnostico; }

    public List<EntradaHistorial> getEntradasHistorial() { return entradasHistorial; }
    public void setEntradasHistorial(List<EntradaHistorial> entradasHistorial) { this.entradasHistorial = entradasHistorial; }
    public List<Reporte> getReportesGenerados() { return reportesGenerados; }
    public void setReportesGenerados(List<Reporte> reportesGenerados) { this.reportesGenerados = reportesGenerados; }
}