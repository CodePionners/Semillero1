package org.sistemadegestiondelesionescutaneas.model;

public enum edadestimada{
public String Edad clinica; //Basada en la apariencia de la lesion y como ha cambiado en el tiempo"),
public String Edad biologica;// "Basada en cambios celulares que se pueden observar en pruebas especificas");

public final String descripcion;

public edadestimada(String descripcion) {
    this.descripcion = descripcion;
}

public String getDescripcion() {
    return descripcion;
}
