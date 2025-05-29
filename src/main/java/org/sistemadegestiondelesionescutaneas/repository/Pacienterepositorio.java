package org.sistemadegestiondelesionescutaneas.repository;

import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.springframework.data.domain.Sort; // Importar Sort
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Importar Query
import java.util.List; // Importar List
import java.util.Optional;

public interface Pacienterepositorio extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByIdentificacion(String identificacion);

    // NUEVO MÉTODO para cargar pacientes con sus análisis y diagnósticos (del primer análisis)
    // Usamos LEFT JOIN FETCH para incluir pacientes incluso si no tienen análisis.
    // Y para el diagnóstico del primer análisis (si existe).
    @Query("SELECT DISTINCT p FROM Paciente p LEFT JOIN FETCH p.analisis a LEFT JOIN FETCH a.diagnostico ORDER BY p.nombre ASC")
    List<Paciente> findAllWithAnalisisAndDiagnosticoOrderedByName();

    // Si necesitas el Sort como parámetro:
    @Query("SELECT DISTINCT p FROM Paciente p LEFT JOIN FETCH p.analisis a LEFT JOIN FETCH a.diagnostico")
    List<Paciente> findAllWithAnalisisAndDiagnostico(Sort sort);

}