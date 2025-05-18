package org.sistemadegestiondelesionescutaneas.model;
// Renombrar archivo a TamanoLesion.java
public enum TamanoLesion {
    MENOR_A_0_5_CM("Menor a 0.5 cm"), // ej. < 5mm
    ENTRE_0_5_Y_1_CM("Entre 0.5 cm y 1 cm"),
    ENTRE_1_Y_2_CM("Entre 1 cm y 2 cm"),
    MAYOR_A_2_CM("Mayor a 2 cm"),
    VARIABLE_O_MULTIPLE("Variable o múltiples tamaños"),
    NO_MEDIDO("No medido / No especificado");

    private final String descripcion;

    TamanoLesion(String descripcion) { // El constructor de enum es implícitamente privado
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
