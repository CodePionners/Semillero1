package org.sistemadegestiondelesionescutaneas.model;

import java.time.LocalDateTime;

public class Reporte {
    private Integer id;
    private Integer id_analisis_referencia;
    private Paciente paciente; // Relación Muchos a Uno

    private LocalDateTime fechahorageneracion; //MODIFICADO
    private String tiporeporte; // O Enum //MODIFICADO
    private String rutaarchivo; //MODIFICADO
    private AnalisisDermatologico analisisreferencia; // Relación Muchos a Uno/Cero MODIFICADO
}
