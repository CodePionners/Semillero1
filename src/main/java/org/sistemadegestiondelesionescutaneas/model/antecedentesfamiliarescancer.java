package org.sistemadegestiondelesionescutaneas.model;

private enum antecedentesfamiliarescancer{
Tipo("Carsinoma basocelular, carsinoma espinocelular, melanoma"),
Parentesco("Primer grado, segundo grado"),//Primer padres, hermanos, hijos. Segundo abuelos, tios y primos.
Edad diagnostico("")//Edad que los familiares fueron diagnosticados,
Tratamientos("Cirugia, crioterapia, radioterapia, terapia topica, electrodesecacion y curetraje, inmunoterapia, quimioteria, terapia dirigida");


public final String descripcion;

private antecedentesfamiliarescancer(String descripcion) {
    this.descripcion = descripcion;
}

public String getDescripcion() {
    return descripcion;
}
}
