package org.sistemadegestiondelesionescutaneas.model;

public Cons areacorporalafectada {
    Cabeza y cuello ("Cuero cabelludo, cara, cuello y orejas"),
    Tronco("Pecho,abdomen, espalda y region lumbar"),
    Extremidades superiores("Brazos, manos y muñecas"),
    Extremidades inferiores("Piernas, pies y tobillos"),
    Genitales("Area genital externa"),
    Pliegues("Axilas,ingles y debajo de los senos")
    Zona perioral("Alrededor de la boca"),
    Zona periorbital("Alrededor de los ojos");

    private final String descripcion;

    private areacorporalafectada(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
