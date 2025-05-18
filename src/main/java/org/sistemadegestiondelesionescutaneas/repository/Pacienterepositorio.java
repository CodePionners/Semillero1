package org.sistemadegestiondelesionescutaneas.repository;
import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
public interface Pacienterepositorio extends JpaRepository<Paciente, Long> {}