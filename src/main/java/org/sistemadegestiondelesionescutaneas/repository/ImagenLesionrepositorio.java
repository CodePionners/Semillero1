package org.sistemadegestiondelesionescutaneas.repository;

import org.sistemadegestiondelesionescutaneas.model.ImagenLesion;
import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime; // Necesario para ImagenInfo
import java.util.List;
import java.util.Optional;

// Interfaz de Proyección para información básica de la imagen
interface ImagenInfo {
    Long getId();
    String getRutaArchivo();
    LocalDateTime getFechaSubida();
}

public interface ImagenLesionrepositorio extends JpaRepository<ImagenLesion, Long> {

    List<ImagenLesion> findByPacienteOrderByFechaSubidaDesc(Paciente paciente); //

    /**
     * Obtiene una ImagenLesion por su ID, junto con el Paciente y Usuario asociados.
     * Esto previene problemas N+1 al acceder a imagen.getPaciente().getUsuario().
     * @param imagenId El ID de la ImagenLesion a buscar.
     * @return Un Optional conteniendo la ImagenLesion si se encuentra.
     */
    @Query("SELECT il FROM ImagenLesion il JOIN FETCH il.paciente p JOIN FETCH p.usuario u WHERE il.id = :imagenId")
    Optional<ImagenLesion> findByIdWithPacienteAndUsuario(@Param("imagenId") Long imagenId);

    /**
     * Obtiene una lista de información básica de imágenes para un paciente, ordenadas por fecha de subida descendente.
     * Utiliza una proyección para obtener solo los campos necesarios.
     * @param paciente El paciente para el cual obtener el historial de imágenes.
     * @return Una lista de ImagenInfo.
     */
    @Query("SELECT il.id as id, il.rutaArchivo as rutaArchivo, il.fechaSubida as fechaSubida FROM ImagenLesion il WHERE il.paciente = :paciente ORDER BY il.fechaSubida DESC")
    List<ImagenInfo> findProjectedByPacienteOrderByFechaSubidaDesc(@Param("paciente") Paciente paciente);
}