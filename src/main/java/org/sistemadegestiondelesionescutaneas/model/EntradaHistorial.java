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

    // Columna 'evento' que coincide con el error de la BD
    @Column(name = "evento", nullable = false, length = 100) // Asegúrate que 'nullable = false' si la BD lo exige
    private String evento;

    @Column(length = 1000)
    private String detalles;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte", length = 50)
    private TipoReporte tipoReporte;

    @Enumerated(EnumType.STRING)
    @Column(name = "diagnostico_entrada", length = 50)
    private Diagnostico diagnostico;

    @Column(name = "estado_original", length = 50) // Mantenemos el campo original 'estado' por si se usa o para migración
    private String estadoOriginal;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_analisis_referencia")
    private AnalisisDermatologico analisisreferencia;

    public EntradaHistorial() {
        this.fechaHora = LocalDateTime.now();
    }

    // Constructor actualizado
    public EntradaHistorial(Paciente paciente, TipoReporte tipoReporte, Diagnostico diagnostico, String detallesAdicionales) {
        this();
        this.paciente = paciente;
        this.tipoReporte = tipoReporte;
        this.diagnostico = diagnostico;
        this.detalles = detallesAdicionales;

        if (tipoReporte != null) {
            this.evento = tipoReporte.getDescripcion(); // Asignar valor a 'evento'
        } else {
            this.evento = "Actualización General"; // O un valor por defecto si tipoReporte puede ser null
        }

        if (diagnostico != null) {
            this.estadoOriginal = diagnostico.getDescripcion(); // Poblar 'estadoOriginal' si es necesario
        }
    }


    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getEvento() { return evento; } // Getter para el campo 'evento'
    public void setEvento(String evento) { this.evento = evento; } // Setter para el campo 'evento'

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    public TipoReporte getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(TipoReporte tipoReporte) { this.tipoReporte = tipoReporte; }

    public Diagnostico getDiagnostico() { return diagnostico; }
    public void setDiagnostico(Diagnostico diagnostico) { this.diagnostico = diagnostico; }

    public AnalisisDermatologico getAnalisisreferencia() { return analisisreferencia; }
    public void setAnalisisreferencia(AnalisisDermatologico analisisreferencia) { this.analisisreferencia = analisisreferencia; }

    public String getEstadoOriginal() { return estadoOriginal; }
    public void setEstadoOriginal(String estadoOriginal) { this.estadoOriginal = estadoOriginal; }
}
