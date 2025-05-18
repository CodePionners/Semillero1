package org.sistemadegestiondelesionescutaneas.model;

import java.time.LocalDateTime;

public class Historiaclinica {
    private Integer id;
    private Integer id_analisis_referencia;
    private Paciente paciente; // Relación Muchos a Uno

    private LocalDateTime fechaHora;
    private String evento; // O Enum
    private String detalles;
    private String estado; // O Enum
}
