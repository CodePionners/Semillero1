package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "imagenes_lesion", indexes = {
        @Index(name = "idx_imagenlesion_paciente_fechasubida", columnList = "id_paciente, fechaSubida DESC")
})
public class ImagenLesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen_lesion")
    private Long id;

    @Column(nullable = false)
    private String rutaArchivo;

    @Column(nullable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @OneToOne(mappedBy = "imagen", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private AnalisisDermatologico analisisDermatologico;

    public ImagenLesion() {}

    // Getters y Setters
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

    @Override
    public String toString() {
        return "ImagenLesion{" + "id=" + id + ", rutaArchivo='" + rutaArchivo + '\'' + ", fechaSubida=" + fechaSubida + ", pacienteId=" + (paciente != null ? paciente.getId() : "null") + ", analisisDermatologicoId=" + (analisisDermatologico != null && analisisDermatologico.getId() != null ? analisisDermatologico.getId() : "null") + '}';
    }
}