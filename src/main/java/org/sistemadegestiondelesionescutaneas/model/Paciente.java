package org.sistemadegestiondelesionescutaneas.model;

public class Paciente {
    private String id;
    private String nombre;
    private Integer edad;
    private String Sexo;
    private List<ImagenLesion> imagenes;
    private List<AnalisisDermatologico> analisis;
    private List<EntradaHistorial> historial;
    private List<Reporte> reportes;
}
