package org.sistemadegestiondelesionescutaneas.model;

public enum TipoReporte {
    SEGUIMIENTO("Reporte de Seguimiento"),
    DIAGNOSTICO_INICIAL("Reporte de Diagnóstico Inicial"),
    INTERCONSULTA("Reporte de Interconsulta"),
    HISTORIAL_COMPLETO("Reporte de Historial Completo");

    private final String descripcion;

    TipoReporte(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
