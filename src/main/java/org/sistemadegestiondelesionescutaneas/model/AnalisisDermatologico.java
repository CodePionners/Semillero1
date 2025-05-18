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

    @ManyToOne(fetch = FetchType.LAZY, optional = false) //Cada análisis puede tener muchas referencias
    @JoinColumn(name = "id_analisis_referencia", nullable = false)

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Cada análisis pertenece a un paciente
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @OneToOne(fetch = FetchType.LAZY, optional = false) // Cada análisis es sobre una imagen
    @JoinColumn(name = "id_imagen_lesion", nullable = false, unique = true)
    private ImagenLesion imagen;

    @Column(nullable = false)
    private LocalDateTime fechahoraanalisis;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EdadEstimada edadestimada;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private AreaCorporalAfectada areacorporalafectada;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private TipoPielFitzpatrick tipopielfitzpatrick;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private Tamanodelesion tamanodelalesion;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private AntecedentesFamiliaresCancer antecedentesfamiliarescancer;

    private Boolean historiallesionesprevias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Diagnostico diagnostico;

    @OneToMany(mappedBy = "id_analisis_referencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Historiaclinica> id_analisis_referencia;

    @OneToMany(mappedBy = "id_analisis_referencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reporte> id_analisis_referencia;

    public AnalisisDermatologico() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getId_analisis_referencia() { return id_analisis_referencia; }
    public void setId_analisis_referencia(Long id_analisis_referencia) { this.id_analisis_referencia = id_analisis_referencia; }
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
    public Tamanodelesion getTamanodelalesion() { return tamanodelalesion; }
    public void setTamanodelalesion(Tamanodelesion tamanodelalesion) { this.tamanodelalesion = tamanodelalesion; }
    public AntecedentesFamiliaresCancer getAntecedentesfamiliarescancer() { return antecedentesfamiliarescancer; }
    public void setAntecedentesfamiliarescancer(AntecedentesFamiliaresCancer antecedentesfamiliarescancer) { this.antecedentesfamiliarescancer = antecedentesfamiliarescancer; }
    public Boolean getHistoriallesionesprevias() { return historiallesionesprevias; }
    public void setHistoriallesionesprevias(Boolean historiallesionesprevias) { this.historiallesionesprevias = historiallesionesprevias; }
    public Diagnostico getDiagnostico() { return diagnostico; }
    public void setDiagnostico(Diagnostico diagnostico) { this.diagnostico = diagnostico; }

    // public List<Historiaclinica> getHistoriaClinica() { return HistoriaClinica; }
    // public void setHistoriaClinica(List<Historiaclinica> id_analisis_reporte) { this.entradasHistorial = entradasHistorial; }
    // public List<Reporte> getReportesGenerados() { return reportesGenerados; }
    // public void setReportesGenerados(List<Reporte> reportesGenerados) { this.reportesGenerados = reportesGenerados; }
}