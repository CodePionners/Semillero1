package org.sistemadegestiondelesionescutaneas.model;

public Cons tamanodelesion{
    Diametro("La medida de la lesion de un lado a otro"),
    Superficie("El area total que ocupa la lesion en la piel"),
    Profundidad("La extension de la lesion hacia dentro de la piel"),
    Numero("La cantidad de lesiones que aparecen en una area en especifica")

private final String descripcion;

private tamanodelesion(String descripcion) {
    this.descripcion = descripcion;
}

public String getDescripcion() {
    return descripcion;
}

