package org.sistemadegestiondelesionescutaneas.model;

import jakarta.persistence.*; // Usar jakarta.persistence para Spring Boot 3+

import java.time.LocalDateTime; // Para la fecha y hora

@Entity // Indica que esta clase es una entidad JPA
@Table(name = "lesion_images") // Especifica el nombre de la tabla en la base de datos
public class LesionImage {

    @Id // Marca este campo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la auto-generación del ID por la base de datos
    private Long id; // Tipo Long es común para IDs de DB autoincrementales

    @Column(nullable = false) // Indica que esta columna no puede ser nula
    private String filename; // Nombre original del archivo subido

    @Column(nullable = false)
    private String filepath; // Ruta donde se almacena el archivo físicamente en el servidor

    private LocalDateTime uploadDate; // Fecha y hora de subida de la imagen

    // Puedes añadir otros campos para almacenar atributos visuales extraídos o resultados simples aquí
    // O crear otra entidad separada (AnalysisResult) con una relación (OneToOne/OneToMany)
    // Ejemplo simple de campos adicionales (puedes añadirlos o no por ahora):
    // private String colorAnalysis; // Ej: "Rojo predominante"
    // private String shapeAnalysis; // Ej: "Irregular"
    // private String initialClassification; // Ej: "Necesita revisión", "Benigno (probable)"


    // --- Constructores ---
    // JPA requiere un constructor sin argumentos (público o protegido)
    public LesionImage() {
    }

    // Constructor útil para crear objetos antes de guardarlos
    public LesionImage(String filename, String filepath, LocalDateTime uploadDate) {
        this.filename = filename;
        this.filepath = filepath;
        this.uploadDate = uploadDate;
    }


    // --- Getters y Setters ---
    // Necesarios para que JPA acceda a los campos. IntelliJ puede generarlos automáticamente.
    // Haz clic derecho en el código -> Generate -> Getter and Setter -> Selecciona todos los campos.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        return filepath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    // Opcional: toString() para facilitar la depuración
    @Override
    public String toString() {
        return "LesionImage{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", filepath='" + filepath + '\'' +
                ", uploadDate=" + uploadDate +
                '}';
    }
}