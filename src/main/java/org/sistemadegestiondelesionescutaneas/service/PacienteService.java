package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);
    private final Pacienterepositorio pacienteRepositorio;

    @Autowired
    public PacienteService(Pacienterepositorio pacienteRepositorio) {
        this.pacienteRepositorio = pacienteRepositorio;
    }

    @Transactional(readOnly = true)
    public Optional<Paciente> findPacienteCompletoByIdentificacion(String identificacion) {
        Optional<Paciente> pacienteOpt = pacienteRepositorio.findByIdentificacion(identificacion);
        pacienteOpt.ifPresent(paciente -> {
            logger.info("Paciente {} encontrado. Inicializando colecciones...", paciente.getNombre());
            Hibernate.initialize(paciente.getImagenes());
            Hibernate.initialize(paciente.getAnalisis());
            if (paciente.getImagenes() != null) {
                paciente.getImagenes().forEach(imagen -> Hibernate.initialize(imagen.getAnalisisDermatologico()));
            }
            logger.info("Colecciones para paciente {} inicializadas.", paciente.getNombre());
        });
        return pacienteOpt;
    }

    @Transactional(readOnly = true)
    public List<Paciente> findAllPacientesConInfoBasica() {
        return pacienteRepositorio.findAll();
    }

    @Transactional
    public Paciente updatePaciente(Paciente pacienteActualizado) {
        // Cargar el paciente existente para asegurar que estamos actualizando una entidad gestionada
        Paciente pacienteExistente = pacienteRepositorio.findById(pacienteActualizado.getId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + pacienteActualizado.getId()));

        // Actualizar solo los campos que el médico puede modificar desde este formulario
        pacienteExistente.setNombre(pacienteActualizado.getNombre()); // Asumiendo que el nombre también puede ser corregido
        pacienteExistente.setEdad(pacienteActualizado.getEdad());
        pacienteExistente.setSexo(pacienteActualizado.getSexo());
        pacienteExistente.setEdadEstimadaLesion(pacienteActualizado.getEdadEstimadaLesion());
        pacienteExistente.setAreaCorporalAfectadaPredominante(pacienteActualizado.getAreaCorporalAfectadaPredominante());
        pacienteExistente.setTipoPielFitzpatrick(pacienteActualizado.getTipoPielFitzpatrick());
        pacienteExistente.setTamanodeLesionGeneral(pacienteActualizado.getTamanodeLesionGeneral());
        pacienteExistente.setAntecedentesFamiliaresCancer(pacienteActualizado.getAntecedentesFamiliaresCancer());
        // No actualizamos identificacion, usuario, ni colecciones desde este método simple

        return pacienteRepositorio.save(pacienteExistente);
    }
}