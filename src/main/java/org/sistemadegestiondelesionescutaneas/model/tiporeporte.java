package org.sistemadegestiondelesionescutaneas.model;

public Cons tiporeporte {
       Seguimiento("Reporte Seguimiento"),
       Diagnostico("Reporte Diagnostico");

private final String descripcion;

private tiporeporte(String descripcion) {
    this.descripcion = descripcion;
}

public String getDescripcion() {
    return descripcion;
}



}
