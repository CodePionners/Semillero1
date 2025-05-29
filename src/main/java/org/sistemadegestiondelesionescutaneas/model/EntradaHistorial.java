package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historia_clinica_entradas")
public class EntradaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada_historial")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 100)
    private String evento;

    @Column(length = 1000)
    private String detalles;

    @Column(length = 50)
    private String estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_analisis_referencia")
    private AnalisisDermatologico analisisreferencia;

    public EntradaHistorial() {
        this.fechaHora = LocalDateTime.now();
    }

    public EntradaHistorial(Paciente paciente, String evento, String detalles, String estado) {
        this();
        this.paciente = paciente;
        this.evento = evento;
        this.detalles = detalles;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public AnalisisDermatologico getAnalisisreferencia() { return analisisreferencia; }
    public void setAnalisisreferencia(AnalisisDermatologico analisisreferencia) { this.analisisreferencia = analisisreferencia; }
}