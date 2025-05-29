package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "analisis_dermatologicos")
public class AnalisisDermatologico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_analisis")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_imagen_lesion", nullable = false, unique = true)
    private ImagenLesion imagen;

    @Column(nullable = false)
    private LocalDateTime fechahoraanalisis;

    @Enumerated(EnumType.STRING) @Column(length = 20) private Sexo sexo;
    @Enumerated(EnumType.STRING) @Column(length = 50) private EdadEstimada edadestimada;
    @Enumerated(EnumType.STRING) @Column(length = 100) private AreaCorporalAfectada areacorporalafectada;
    @Enumerated(EnumType.STRING) @Column(length = 100) private TipoPielFitzpatrick tipopielfitzpatrick;
    @Enumerated(EnumType.STRING) @Column(length = 100) private TamanodeLesion tamanodelesion;
    @Enumerated(EnumType.STRING) @Column(length = 100) private AntecedentesFamiliaresCancer antecedentesfamiliarescancer;
    private Boolean historiallesionesprevias;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50) private Diagnostico diagnostico;

    @OneToMany(mappedBy = "analisisreferencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<EntradaHistorial> entradasHistorial = new HashSet<>();

    public AnalisisDermatologico() {}

    // Getters y Setters (sin cambios en firma, tipos Set correctos)
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
    public Set<EntradaHistorial> getEntradasHistorial() { return entradasHistorial; }
    public void setEntradasHistorial(Set<EntradaHistorial> entradasHistorial) { this.entradasHistorial = entradasHistorial; }
}