package org.sistemadegestiondelesionescutaneas.model;

import java.time.LocalDateTime;

public class AnalisisDermatologico {
    private String id;
    private Paciente paciente; // Relación Muchos a Uno
    private ImagenLesion imagen; // Relación Uno a Uno

    private LocalDateTime fechahoraanalisis; //MODIFICADO

    // Atributos Clínicos
    private String Sexo;
    private String edadestimada;
    private String areacorporalafectada; // O Enum
    private String tipodepielfitzpatrick; // O Enum
    private String tamanodelalesion;
    private String antecedentesfamiliarescancer;
    private Boolean historiallesionesprevias;

    private String diagnostico; // O Enum MODIFICADO
}
