package org.sistemadegestiondelesionescutaneas.repository;
import org.sistemadegestiondelesionescutaneas.model.ImagenLesion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImagenLesionrepositorio extends JpaRepository<ImagenLesion, Long> {}
List<ImagenLesion> findByPacienteOrderByFechaSubidaDesc(Paciente paciente);
