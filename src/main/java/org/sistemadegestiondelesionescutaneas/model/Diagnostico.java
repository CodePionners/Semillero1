package org.sistemadegestiondelesionescutaneas.model;

private enum Diagnostico{
    Benigna("Benigna"),
    Maligna("Maligna"),
    Indeterminado("Indeterminado");

    private final String descripcion;

    private Diagnostico(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

