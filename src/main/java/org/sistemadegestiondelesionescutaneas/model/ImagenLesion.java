package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "imagenes_lesion")
public class ImagenLesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen_lesion")
    private Long id; // Cambiado de String a Long

    @Column(nullable = false)
    private String rutaArchivo;

    @Column(nullable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false) // FK
    private Paciente paciente;

    @OneToOne(mappedBy = "imagen", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AnalisisDermatologico analisisDermatologico;

    public ImagenLesion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public AnalisisDermatologico getAnalisisDermatologico() { return analisisDermatologico; }
    public void setAnalisisDermatologico(AnalisisDermatologico analisisDermatologico) { this.analisisDermatologico = analisisDermatologico; }
}