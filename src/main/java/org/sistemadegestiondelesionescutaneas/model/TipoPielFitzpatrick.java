package org.sistemadegestiondelesionescutaneas.model;

public enum TipoPielFitzpatrick {
    I("Piel muy clara, siempre se quema, nunca se broncea"),
    II("Piel clara, se quema fácilmente, se broncea con dificultad"),
    III("Piel clara a ligeramente oscura, se quema ocasionalmente, se broncea gradualmente"),
    IV("Piel moderadamente oscura, se quema mínimamente, se broncea con facilidad"),
    V("Piel oscura, rara vez se quema, se broncea intensamente"),
    VI("Piel muy oscura, nunca se quema, siempre se broncea"),
    NO_ESPECIFICADO("No especificado");

    private final String descripcion;

    TipoPielFitzpatrick(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}