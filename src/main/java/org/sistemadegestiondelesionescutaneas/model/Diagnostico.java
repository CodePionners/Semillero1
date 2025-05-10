package org.sistemadegestiondelesionescutaneas.model;

public Cons Diagnostico{
    Benigna("Benigna"),
    Sospechosa("Sospechosa de malignidad"),
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

