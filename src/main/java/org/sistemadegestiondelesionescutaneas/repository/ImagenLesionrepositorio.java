package org.sistemadegestiondelesionescutaneas.repository;

import org.sistemadegestiondelesionescutaneas.model.ImagenLesion;
import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.springframework.data.domain.Sort; // Importar Sort
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Interfaz de Proyección para información básica de la imagen (se mantiene por si se usa en otro lado)
interface ImagenInfo {
    Long getId();
    String getRutaArchivo();
    LocalDateTime getFechaSubida();
}

public interface ImagenLesionrepositorio extends JpaRepository<ImagenLesion, Long> {

    List<ImagenLesion> findByPacienteOrderByFechaSubidaDesc(Paciente paciente);

    @Query("SELECT il FROM ImagenLesion il JOIN FETCH il.paciente p JOIN FETCH p.usuario u WHERE il.id = :imagenId")
    Optional<ImagenLesion> findByIdWithPacienteAndUsuario(@Param("imagenId") Long imagenId);

    @Query("SELECT il.id as id, il.rutaArchivo as rutaArchivo, il.fechaSubida as fechaSubida FROM ImagenLesion il WHERE il.paciente = :paciente ORDER BY il.fechaSubida DESC")
    List<ImagenInfo> findProjectedByPacienteOrderByFechaSubidaDesc(@Param("paciente") Paciente paciente);

    // NUEVO MÉTODO para cargar todas las imágenes con sus pacientes (para la galería del médico)
    @Query("SELECT DISTINCT il FROM ImagenLesion il JOIN FETCH il.paciente ORDER BY il.fechaSubida DESC")
    List<ImagenLesion> findAllWithPacienteOrderByFechaSubidaDesc();

    // Si prefieres usar Sort en lugar de hardcodear el ORDER BY en la query:
    // @Query("SELECT DISTINCT il FROM ImagenLesion il JOIN FETCH il.paciente")
    // List<ImagenLesion> findAllWithPaciente(Sort sort);

}