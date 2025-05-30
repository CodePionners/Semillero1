package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.EntradaHistorial;
import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment; // Corregido
import com.itextpdf.layout.properties.UnitValue;     // Corregido

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException; // Asegúrate que esta importación esté presente
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime; // Añadido
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

@Service
public class HistorialExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public ByteArrayInputStream generarHistorialPdf(Paciente paciente) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            if (paciente == null) {
                document.add(new Paragraph("No se proporcionó información del paciente.").setTextAlignment(TextAlignment.CENTER));
                return new ByteArrayInputStream(baos.toByteArray());
            }

            document.add(new Paragraph("Historial Clínico del Paciente")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18)
                    .setMarginBottom(10));

            document.add(new Paragraph("Nombre: " + (paciente.getNombre() != null ? paciente.getNombre() : "N/A")).setMarginBottom(2));
            document.add(new Paragraph("Identificación: " + (paciente.getIdentificacion() != null ? paciente.getIdentificacion() : "N/A")).setMarginBottom(2));
            document.add(new Paragraph("Sexo: " + (paciente.getSexo() != null ? paciente.getSexo().getDescripcion() : "N/A")).setMarginBottom(2));
            document.add(new Paragraph("Edad: " + (paciente.getEdad() != null ? paciente.getEdad().toString() + " años" : "N/A")).setMarginBottom(2));
            document.add(new Paragraph("Fecha de Exportación: " + LocalDateTime.now().format(DATE_TIME_FORMATTER)).setMarginBottom(15));


            if (paciente.getHistorial() != null && !paciente.getHistorial().isEmpty()) {
                Table table = new Table(UnitValue.createPercentArray(new float[]{2f, 2.5f, 2.5f, 5f}));
                table.setWidth(UnitValue.createPercentValue(100));

                table.addHeaderCell(new Cell().add(new Paragraph("Fecha").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addHeaderCell(new Cell().add(new Paragraph("Tipo de Reporte").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addHeaderCell(new Cell().add(new Paragraph("Diagnóstico").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addHeaderCell(new Cell().add(new Paragraph("Notas Adicionales / Detalles Clínicos").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));

                for (EntradaHistorial entrada : paciente.getHistorial()) {
                    StringJoiner detallesClinicos = new StringJoiner("\n");
                    if (paciente.getEdadEstimadaLesion() != null) {
                        detallesClinicos.add("Edad Estimada Lesión: " + paciente.getEdadEstimadaLesion().getDescripcion());
                    }
                    if (paciente.getAreaCorporalAfectadaPredominante() != null) {
                        detallesClinicos.add("Área Corporal: " + paciente.getAreaCorporalAfectadaPredominante().getDescripcion());
                    }
                    if (paciente.getTipoPielFitzpatrick() != null) {
                        detallesClinicos.add("Tipo Piel: " + paciente.getTipoPielFitzpatrick().getDescripcion());
                    }
                    if (paciente.getTamanodeLesionGeneral() != null) {
                        detallesClinicos.add("Tamaño Lesión: " + paciente.getTamanodeLesionGeneral().getDescripcion());
                    }
                    if (paciente.getAntecedentesFamiliaresCancer() != null) {
                        detallesClinicos.add("Antecedentes Cáncer: " + paciente.getAntecedentesFamiliaresCancer().getDescripcion());
                    }
                    if (entrada.getDetalles() != null && !entrada.getDetalles().trim().isEmpty()) {
                        detallesClinicos.add("Motivo/Notas Entrada: " + entrada.getDetalles());
                    }

                    table.addCell(new Cell().add(new Paragraph(entrada.getFechaHora() != null ? entrada.getFechaHora().format(DATE_TIME_FORMATTER) : "N/A")));
                    table.addCell(new Cell().add(new Paragraph(entrada.getTipoReporte() != null ? entrada.getTipoReporte().getDescripcion() : "N/A")));
                    table.addCell(new Cell().add(new Paragraph(entrada.getDiagnostico() != null ? entrada.getDiagnostico().getDescripcion() : "N/A")));
                    table.addCell(new Cell().add(new Paragraph(detallesClinicos.toString().isEmpty() ? "N/A" : detallesClinicos.toString()).setFontSize(9)));
                }
                document.add(table);
            } else {
                document.add(new Paragraph("No hay entradas en el historial para este paciente.").setItalic());
            }
        } catch (IOException ioe) {
            System.err.println("IOException al generar PDF principal: " + ioe.getMessage());
            ioe.printStackTrace();
            return generarPdfDeError("Error de E/S generando el documento PDF: " + ioe.getMessage());
        } catch (Exception e) {
            System.err.println("Error generando PDF (general): " + e.getMessage());
            e.printStackTrace();
            return generarPdfDeError("Error generando el documento PDF: " + e.getMessage());
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private ByteArrayInputStream generarPdfDeError(String mensajeErrorExterno) {
        ByteArrayOutputStream baosError = new ByteArrayOutputStream();
        try (PdfWriter errorWriter = new PdfWriter(baosError);
             PdfDocument errorPdf = new PdfDocument(errorWriter);
             Document errorDocument = new Document(errorPdf)) {
            errorDocument.add(new Paragraph("Error al generar el PDF").setBold().setFontColor(ColorConstants.RED));
            errorDocument.add(new Paragraph(mensajeErrorExterno != null ? mensajeErrorExterno : "Ocurrió un error desconocido."));
        } catch (IOException ioe) {
            System.err.println("IOException DENTRO de generarPdfDeError: " + ioe.getMessage());
            ioe.printStackTrace();
            return new ByteArrayInputStream(("Fallo crítico (IOException) al generar PDF de error: " + ioe.getMessage()).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            System.err.println("Exception DENTRO de generarPdfDeError: " + ex.getMessage());
            ex.printStackTrace();
            return new ByteArrayInputStream(("Fallo crítico (inesperado) al generar PDF de error: " + ex.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
        return new ByteArrayInputStream(baosError.toByteArray());
    }

    public ByteArrayInputStream generarHistorialCsv(Paciente paciente) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(osw)) {

            if (paciente != null) {
                pw.println(formatCsvField("Historial Clínico del Paciente"));
                pw.println(formatCsvField("Nombre:") + "," + formatCsvField(paciente.getNombre()));
                pw.println(formatCsvField("Identificación:") + "," + formatCsvField(paciente.getIdentificacion()));
                pw.println(formatCsvField("Sexo:") + "," + formatCsvField(paciente.getSexo() != null ? paciente.getSexo().getDescripcion() : "N/A"));
                pw.println(formatCsvField("Edad:") + "," + formatCsvField(paciente.getEdad() != null ? paciente.getEdad().toString() + " años" : "N/A"));
                pw.println(formatCsvField("Fecha de Exportación:") + "," + formatCsvField(LocalDateTime.now().format(DATE_TIME_FORMATTER)));
                pw.println();
            } else {
                pw.println(formatCsvField("No se proporcionó información del paciente."));
                pw.println();
            }

            pw.println("ID_Entrada,FechaHora,Tipo de Reporte,Diagnostico,Notas Adicionales / Detalles Clínicos");

            if (paciente != null && paciente.getHistorial() != null && !paciente.getHistorial().isEmpty()) {
                for (EntradaHistorial entrada : paciente.getHistorial()) {
                    StringJoiner detallesClinicosCsv = new StringJoiner("; ");
                    if (paciente.getEdadEstimadaLesion() != null) {
                        detallesClinicosCsv.add("Edad Estimada Lesión: " + paciente.getEdadEstimadaLesion().getDescripcion());
                    }
                    if (paciente.getAreaCorporalAfectadaPredominante() != null) {
                        detallesClinicosCsv.add("Área Corporal: " + paciente.getAreaCorporalAfectadaPredominante().getDescripcion());
                    }
                    if (paciente.getTipoPielFitzpatrick() != null) {
                        detallesClinicosCsv.add("Tipo Piel: " + paciente.getTipoPielFitzpatrick().getDescripcion());
                    }
                    if (paciente.getTamanodeLesionGeneral() != null) {
                        detallesClinicosCsv.add("Tamaño Lesión: " + paciente.getTamanodeLesionGeneral().getDescripcion());
                    }
                    if (paciente.getAntecedentesFamiliaresCancer() != null) {
                        detallesClinicosCsv.add("Antecedentes Cáncer: " + paciente.getAntecedentesFamiliaresCancer().getDescripcion());
                    }
                    if (entrada.getDetalles() != null && !entrada.getDetalles().trim().isEmpty()) {
                        detallesClinicosCsv.add("Motivo/Notas Entrada: " + entrada.getDetalles());
                    }

                    pw.printf("%s,%s,%s,%s,%s\n",
                            formatCsvField(entrada.getId() != null ? entrada.getId().toString() : ""),
                            formatCsvField(entrada.getFechaHora() != null ? entrada.getFechaHora().format(DATE_TIME_FORMATTER) : ""),
                            formatCsvField(entrada.getTipoReporte() != null ? entrada.getTipoReporte().getDescripcion() : ""),
                            formatCsvField(entrada.getDiagnostico() != null ? entrada.getDiagnostico().getDescripcion() : ""),
                            formatCsvField(detallesClinicosCsv.toString().isEmpty() ? "N/A" : detallesClinicosCsv.toString())
                    );
                }
            } else {
                pw.println(formatCsvField("N/A") + "," + formatCsvField("N/A") + "," + formatCsvField("N/A") + "," + formatCsvField("N/A") + "," + formatCsvField("No hay datos de historial disponibles para el paciente: " + (paciente != null ? paciente.getNombre() : "N/A")));
            }
            pw.flush();
        } catch (IOException e) {
            System.err.println("Error generando CSV: " + e.getMessage());
            e.printStackTrace();
            return new ByteArrayInputStream(("Error al generar el CSV: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private String formatCsvField(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replace("\"", "\"\"");
        if (data.contains(",") || data.contains("\n") || data.contains("\"")) {
            return "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}
