package org.sistemadegestiondelesionescutaneas.model;

public enum EdadEstimada {
    MENOS_DE_1_MES("Menos de 1 mes"),
    DE_1_A_6_MESES("De 1 a 6 meses"),
    DE_6_MESES_A_1_ANO("De 6 meses a 1 año"),
    MAS_DE_1_ANO("Más de 1 año"),
    CRONICA_LARGA_DATA("Crónica / Larga data"),
    RECIENTE_AGUDA("Reciente / Aguda"),
    NO_APLICA("No aplica"),
    DESCONOCIDA("Desconocida / No especificada");

    private final String descripcion;

    EdadEstimada(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}