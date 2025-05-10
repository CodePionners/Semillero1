package org.sistemadegestiondelesionescutaneas.model;

public Cons Sexo {
    MASCULINO("Masculino"),
    FEMENINO("Femenino"),
    OTRO("Otro / No especificado");

    private final String descripcion;

    // Constructor para asociar la descripción a cada constante
    private Sexo(String descripcion) {
        this.descripcion = descripcion;
    }

    // Getter para obtener la descripción
    public String getDescripcion() {
        return descripcion;
    }
}

}
