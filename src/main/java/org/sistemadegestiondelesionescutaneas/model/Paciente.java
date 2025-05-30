package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "pacientes", indexes = {
        @Index(name = "idx_paciente_identificacion", columnList = "identificacion", unique = true)
})
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;

    // NUEVOS CAMPOS PARA EL FORMULARIO Y ÚLTIMO ESTADO CONOCIDO
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte_actual", length = 50)
    private TipoReporte tipoReporteActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "diagnostico_predominante", length = 50)
    private Diagnostico diagnosticoPredominante;
    // FIN NUEVOS CAMPOS

    @Column(name = "identificacion", unique = true, nullable = true, length = 50)
    private String identificacion;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EdadEstimada edadEstimadaLesion;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private AreaCorporalAfectada areaCorporalAfectadaPredominante;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private TipoPielFitzpatrick tipoPielFitzpatrick;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private TamanodeLesion tamanodeLesionGeneral;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private AntecedentesFamiliaresCancer antecedentesFamiliaresCancer;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ImagenLesion> imagenes = new HashSet<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AnalisisDermatologico> analisis = new HashSet<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<EntradaHistorial> historial = new HashSet<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<HistorialLesionPrevia> historialLesionesPrevias = new HashSet<>();

    @Transient
    private String motivoConsultaActual; // Para notas adicionales del formulario

    public Paciente() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    // Getters y Setters para nuevos campos
    public TipoReporte getTipoReporteActual() { return tipoReporteActual; }
    public void setTipoReporteActual(TipoReporte tipoReporteActual) { this.tipoReporteActual = tipoReporteActual; }
    public Diagnostico getDiagnosticoPredominante() { return diagnosticoPredominante; }
    public void setDiagnosticoPredominante(Diagnostico diagnosticoPredominante) { this.diagnosticoPredominante = diagnosticoPredominante; }
    // Fin Getters y Setters para nuevos campos

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public EdadEstimada getEdadEstimadaLesion() { return edadEstimadaLesion; }
    public void setEdadEstimadaLesion(EdadEstimada edadEstimadaLesion) { this.edadEstimadaLesion = edadEstimadaLesion; }
    public AreaCorporalAfectada getAreaCorporalAfectadaPredominante() { return areaCorporalAfectadaPredominante; }
    public void setAreaCorporalAfectadaPredominante(AreaCorporalAfectada areaCorporalAfectadaPredominante) { this.areaCorporalAfectadaPredominante = areaCorporalAfectadaPredominante; }
    public TipoPielFitzpatrick getTipoPielFitzpatrick() { return tipoPielFitzpatrick; }
    public void setTipoPielFitzpatrick(TipoPielFitzpatrick tipoPielFitzpatrick) { this.tipoPielFitzpatrick = tipoPielFitzpatrick; }
    public TamanodeLesion getTamanodeLesionGeneral() { return tamanodeLesionGeneral; }
    public void setTamanodeLesionGeneral(TamanodeLesion tamanodeLesionGeneral) { this.tamanodeLesionGeneral = tamanodeLesionGeneral; }
    public AntecedentesFamiliaresCancer getAntecedentesFamiliaresCancer() { return antecedentesFamiliaresCancer; }
    public void setAntecedentesFamiliaresCancer(AntecedentesFamiliaresCancer antecedentesFamiliaresCancer) { this.antecedentesFamiliaresCancer = antecedentesFamiliaresCancer; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Set<ImagenLesion> getImagenes() { return imagenes; }
    public void setImagenes(Set<ImagenLesion> imagenes) { this.imagenes = imagenes; }
    public Set<AnalisisDermatologico> getAnalisis() { return analisis; }
    public void setAnalisis(Set<AnalisisDermatologico> analisis) { this.analisis = analisis; }
    public Set<EntradaHistorial> getHistorial() { return historial; }
    public void setHistorial(Set<EntradaHistorial> historial) { this.historial = historial; }
    public Set<HistorialLesionPrevia> getHistorialLesionesPrevias() { return historialLesionesPrevias; }
    public void setHistorialLesionesPrevias(Set<HistorialLesionPrevia> historialLesionesPrevias) { this.historialLesionesPrevias = historialLesionesPrevias; }
    public String getMotivoConsultaActual() { return motivoConsultaActual; }
    public void setMotivoConsultaActual(String motivoConsultaActual) { this.motivoConsultaActual = motivoConsultaActual; }

    @Override
    public String toString() { return "Paciente{id=" + id + ", nombre='" + nombre + '\'' + ", identificacion='" + identificacion + '\'' + '}'; }
}
