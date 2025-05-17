package org.sistemadegestiondelesionescutaneas.model;
import java.time.LocalDate;

public class hiatoriallesionesprevias {
    public int id_lesion;
    public LocalDate fechainicio;
    public LocalDate fechaderesolucion;
    public String tipodelesion;//Previa y actual
    public String tratamientorecibido;
    public String resultadotratamiento;
    public String complicaciones;
    public String condicionpreexistente;//dermatitis, diabetes
}