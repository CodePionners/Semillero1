package org.sistemadegestiondelesionescutaneas.repository;

import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface Pacienterepositorio extends JpaRepository<Paciente, Long> {

    @EntityGraph(attributePaths = {"usuario"}) // Carga solo el paciente y su usuario
    Optional<Paciente> findByIdentificacion(String identificacion);

    @EntityGraph(attributePaths = {"usuario"}) // Para la lista general, solo usuario por defecto
    @Override
    List<Paciente> findAll();
}