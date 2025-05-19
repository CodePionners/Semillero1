package org.sistemadegestiondelesionescutaneas.model;
public enum TamanodeLesion {
    MENOR_A_0_5_CM("Menor a 0.5 cm"), // ej. < 5mm
    ENTRE_0_5_Y_1_CM("Entre 0.5 cm y 1 cm"),
    ENTRE_1_Y_2_CM("Entre 1 cm y 2 cm"),
    MAYOR_A_2_CM("Mayor a 2 cm"),
    VARIABLE_O_MULTIPLE("Variable o múltiples tamaños"),
    NO_MEDIDO("No medido / No especificado");

    private final String descripcion;

    TamanodeLesion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
