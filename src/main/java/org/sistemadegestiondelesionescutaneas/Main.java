src/
        └── main/
        └── java/
        └── Bean/
        ├── controller/     <-- Lógica de negocio (gestión de imágenes)
           ├── model/          <-- Representación de datos (objetos Lesión, Historial)
           └── view/           <-- Interfaz gráfica (opcional: Swing o consola)
      └── resources/
        └── images/         <-- Aquí se guardarán las imágenes subidas

// Clase modelo para Lesiones
package model;

public class Lesion {
    private String id;
    private String nombreImagen;
    private String rutaImagen;
    private String diagnostico;

    public Lesion(String id, String nombreImagen, String rutaImagen, String diagnostico) {
        this.id = id;
        this.nombreImagen = nombreImagen;
        this.rutaImagen = rutaImagen;
        this.diagnostico = diagnostico;
    }

    // Getters y Setters
}

// Clase para gestionar las imágenes
package controller;

import model.Lesion;
import java.util.ArrayList;
import java.util.List;

public class ImageController {
    private List<Lesion> historial = new ArrayList<>();

    public void cargarImagen(String rutaImagen) {
        // Lógica para cargar imagen y crear objeto Lesion
    }

    public void eliminarImagen(String id) {
        // Lógica para eliminar imagen
    }

    public void modificarImagen(String id, String nuevaRuta) {
        // Lógica para modificar imagen
    }

    public List<Lesion> mostrarHistorial() {
        return historial;
    }
}


