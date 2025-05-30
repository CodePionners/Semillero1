package org.sistemadegestiondelesionescutaneas.model;

public enum Diagnostico {
    BENIGNA("Benigna"),
    MALIGNA("Maligna"),
    INDETERMINADO("Indeterminado"),
    SOSPECHOSO("Sospechoso"), // Podrías añadir más opciones si es necesario
    NO_APLICA("No Aplica");   // Para casos donde no hay un diagnóstico clínico directo

    private final String descripcion;

    Diagnostico(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}