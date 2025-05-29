package org.sistemadegestiondelesionescutaneas.repository;

import org.sistemadegestiondelesionescutaneas.model.AnalisisDermatologico;
import org.sistemadegestiondelesionescutaneas.model.Paciente; // Importar Paciente
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;        // Importar Query
import org.springframework.data.repository.query.Param;   // Importar Param

import java.util.List;
import java.util.Optional; // Importar Optional

public interface AnalisisDermatologicorepositorio extends JpaRepository<AnalisisDermatologico, Long> {
    List<AnalisisDermatologico> findByPaciente_IdOrderByFechahoraanalisisDesc(Long pacienteId);

    // Método para obtener el análisis más reciente de un paciente específico
    @Query("SELECT ad FROM AnalisisDermatologico ad " +
            "LEFT JOIN FETCH ad.paciente p " +
            "LEFT JOIN FETCH ad.imagen i " +
            "WHERE p = :paciente ORDER BY ad.fechahoraanalisis DESC")
    List<AnalisisDermatologico> findByPacienteOrderByFechahoraanalisisDescWithAssociations(@Param("paciente") Paciente paciente);

    // Un método más simple si solo necesitas el último y confías en que las asociaciones se manejen
    // o se carguen explícitamente más tarde si es necesario (o si la entidad principal las tiene EAGER)
    Optional<AnalisisDermatologico> findTopByPacienteOrderByFechahoraanalisisDesc(Paciente paciente);
}