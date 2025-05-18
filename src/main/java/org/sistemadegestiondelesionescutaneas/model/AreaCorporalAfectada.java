package org.sistemadegestiondelesionescutaneas.model;

public enum AreaCorporalAfectada {
    CABEZA_Y_CUELLO("Cuero cabelludo, cara, cuello y orejas"),
    TRONCO("Pecho, abdomen, espalda y región lumbar"),
    EXTREMIDADES_SUPERIORES("Brazos, manos y muñecas"),
    EXTREMIDADES_INFERIORES("Piernas, pies y tobillos"),
    GENITALES("Área genital externa"),
    PLIEGUES("Axilas, ingles y debajo de los senos"),
    ZONA_PERIORAL("Alrededor de la boca"),
    ZONA_PERIORBITAL("Alrededor de los ojos"),
    OTRA("Otra área no listada"),
    NO_ESPECIFICADA("No especificada");

    private final String descripcion;

    AreaCorporalAfectada(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
