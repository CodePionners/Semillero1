package org.sistemadegestiondelesionescutaneas.model;

import java.time.LocalDateTime;

public class Historiaclinica {
    private String id;
    private Paciente paciente; // Relación Muchos a Uno

    private LocalDateTime fechaHora;
    private String evento; // O Enum
    private String detalles;
    private String estado; // O Enum
    private AnalisisDermatologico analisisreferencia; // Relación Muchos a Uno/Cero MODIFICADO
}
