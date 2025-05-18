package org.sistemadegestiondelesionescutaneas.model;

public enum Diagnostico { // Cambiado de private a public
    BENIGNA("Benigna"), // Nota: Corregido el nombre del enum de Benigna a BENIGNA
    MALIGNA("Maligna"), // Nota: Corregido el nombre del enum de Maligna a MALIGNA
    INDETERMINADO("Indeterminado"); // Nota: Corregido el nombre del enum de Indeterminado a INDETERMINADO

    private final String descripcion;

    Diagnostico(String descripcion) { // Constructor es implícitamente privado
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

