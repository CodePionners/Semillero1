package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_analisis_referencia")
    private AnalisisDermatologico analisisReferencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Cada reporte pertenece a un paciente
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoReporte tipoReporte;

    @Column(length = 255)
    private String nombreArchivoGenerado;

    @Lob
    private String contenidoTexto;

    public Reporte() {
        this.fechaGeneracion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnalisisDermatologico getAnalisisReferencia() { return analisisReferencia; }
    public void setAnalisisReferencia(AnalisisDermatologico analisisReferencia) { this.analisisReferencia = analisisReferencia; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public TipoReporte getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(TipoReporte tipoReporte) { this.tipoReporte = tipoReporte; }
    public String getNombreArchivoGenerado() { return nombreArchivoGenerado; }
    public void setNombreArchivoGenerado(String nombreArchivoGenerado) { this.nombreArchivoGenerado = nombreArchivoGenerado; }
    public String getContenidoTexto() { return contenidoTexto; }
    public void setContenidoTexto(String contenidoTexto) { this.contenidoTexto = contenidoTexto; }
}
