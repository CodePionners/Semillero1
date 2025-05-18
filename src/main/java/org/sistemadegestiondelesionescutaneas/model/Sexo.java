package org.sistemadegestiondelesionescutaneas.model;

public enum Sexo { // Cambiado de Cons a enum
    MASCULINO("Masculino"),
    FEMENINO("Femenino"),
    OTRO("Otro / No especificado");

    private final String descripcion;

    // Constructor es privado por defecto en enums
    Sexo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
