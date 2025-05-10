package org.sistemadegestiondelesionescutaneas.model;

public class ImagenLesion {
    private String id;
    private String rutaArchivo;
    private LocalDateTime fechaSubida;
    private Paciente paciente; // Relación Muchos a Uno
}