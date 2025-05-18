package org.sistemadegestiondelesionescutaneas.repository;
import org.sistemadegestiondelesionescutaneas.model.AnalisisDermatologico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AnalisisDermatologicorepositorio extends JpaRepository<AnalisisDermatologico, Long> {
    List<AnalisisDermatologico> findByPaciente_IdOrderByFechahoraanalisisDesc(Long pacienteId);
