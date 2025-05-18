package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historial_lesiones_previas")
public class HistorialLesionPrevia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_lesion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    private LocalDate fechainicio;
    private LocalDate fechaderesolucion;

    @Column(length = 100)
    private String tipodelesion;

    @Column(length = 255)
    private String tratamientorecibido;

    @Column(length = 255)
    private String resultadotratamiento;

    @Lob
    private String complicaciones;

    @Column(length = 255)
    private String condicionpreexistente; //dermatitis, diabetes


    // Constructores, Getters y Setters
    public HistorialLesionPrevia() {}

    // Getters y Setters para todos los campos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public LocalDate getFechainicio() { return fechainicio; }
    public void setFechainicio(LocalDate fechainicio) { this.fechainicio = fechainicio; }
    public LocalDate getFechaderesolucion() { return fechaderesolucion; }
    public void setFechaderesolucion(LocalDate fechaderesolucion) { this.fechaderesolucion = fechaderesolucion; }
    public String getTipodelesion() { return tipodelesion; }
    public void setTipodelesion(String tipodelesion) { this.tipodelesion = tipodelesion; }
    public String getTratamientorecibido() { return tratamientorecibido; }
    public void setTratamientorecibido(String tratamientorecibido) { this.tratamientorecibido = tratamientorecibido; }
    public String getResultadotratamiento() { return resultadotratamiento; }
    public void setResultadotratamiento(String resultadotratamiento) { this.resultadotratamiento = resultadotratamiento; }
    public String getComplicaciones() { return complicaciones; }
    public void setComplicaciones(String complicaciones) { this.complicaciones = complicaciones; }
    public String getCondicionpreexistente() { return condicionpreexistente; }
    public void setCondicionpreexistente(String condicionpreexistente) { this.condicionpreexistente = condicionpreexistente; }
}