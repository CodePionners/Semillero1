package org.sistemadegestiondelesionescutaneas.model;

public enum AntecedentesFamiliaresCancer {
    CARCINOMA_BASOCELULAR("Carcinoma basocelular"),
    CARCINOMA_ESPINOCELULAR("Carcinoma espinocelular"),
    MELANOMA("Melanoma"),
    NINGUNO_CONOCIDO("Ninguno conocido"),
    OTRO("Otro tipo de cáncer familiar"),
    NO_ESPECIFICADO("No especificado");

    private final String descripcion;

    AntecedentesFamiliaresCancer(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

