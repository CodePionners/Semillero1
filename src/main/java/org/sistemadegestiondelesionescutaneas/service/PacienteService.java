package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.EntradaHistorial;
import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.sistemadegestiondelesionescutaneas.repository.EntradaHistorialrepositorio;
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.hibernate.Hibernate; // Descomentar si se usa Hibernate.initialize

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);
    private final Pacienterepositorio pacienteRepositorio;
    private final EntradaHistorialrepositorio entradaHistorialRepositorio;

    @Autowired
    public PacienteService(Pacienterepositorio pacienteRepositorio, EntradaHistorialrepositorio entradaHistorialRepositorio) {
        this.pacienteRepositorio = pacienteRepositorio;
        this.entradaHistorialRepositorio = entradaHistorialRepositorio;
    }

    @Transactional(readOnly = true)
    public Optional<Paciente> findPacienteCompletoByIdentificacion(String identificacion) {
        Optional<Paciente> pacienteOpt = pacienteRepositorio.findByIdentificacion(identificacion);
        pacienteOpt.ifPresent(paciente -> {
            logger.info("Paciente {} (ID: {}) encontrado por identificación '{}'. Historial size: {}. Imágenes size: {}. Análisis size: {}",
                    paciente.getNombre(),
                    paciente.getId(),
                    paciente.getIdentificacion(),
                    paciente.getHistorial() != null ? paciente.getHistorial().size() : "null",
                    paciente.getImagenes() != null ? paciente.getImagenes().size() : "null",
                    paciente.getAnalisis() != null ? paciente.getAnalisis().size() : "null");
        });
        return pacienteOpt;
    }

    @Transactional(readOnly = true)
    public Optional<Paciente> findPacienteCompletoByDbId(Long id) {
        Optional<Paciente> pacienteOpt = pacienteRepositorio.findById(id);
        pacienteOpt.ifPresent(paciente -> {
            logger.info("Paciente {} (ID: {}) encontrado por ID de BD. Historial size: {}. Imágenes size: {}. Análisis size: {}",
                    paciente.getNombre(),
                    paciente.getId(),
                    paciente.getHistorial() != null ? paciente.getHistorial().size() : "null",
                    paciente.getImagenes() != null ? paciente.getImagenes().size() : "null",
                    paciente.getAnalisis() != null ? paciente.getAnalisis().size() : "null");
        });
        return pacienteOpt;
    }

    @Transactional(readOnly = true)
    public List<Paciente> findAllPacientesConInfoBasica() {
        return pacienteRepositorio.findAll();
    }

    @Transactional
    public Paciente updatePaciente(Paciente pacienteConMotivoForm) {
        Paciente pacienteExistente = pacienteRepositorio.findById(pacienteConMotivoForm.getId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + pacienteConMotivoForm.getId()));

        pacienteExistente.setNombre(pacienteConMotivoForm.getNombre());
        pacienteExistente.setEdad(pacienteConMotivoForm.getEdad());
        pacienteExistente.setSexo(pacienteConMotivoForm.getSexo());
        pacienteExistente.setEdadEstimadaLesion(pacienteConMotivoForm.getEdadEstimadaLesion());
        pacienteExistente.setAreaCorporalAfectadaPredominante(pacienteConMotivoForm.getAreaCorporalAfectadaPredominante());
        pacienteExistente.setTipoPielFitzpatrick(pacienteConMotivoForm.getTipoPielFitzpatrick());
        pacienteExistente.setTamanodeLesionGeneral(pacienteConMotivoForm.getTamanodeLesionGeneral());
        pacienteExistente.setAntecedentesFamiliaresCancer(pacienteConMotivoForm.getAntecedentesFamiliaresCancer());

        Paciente pacienteGuardado = pacienteRepositorio.save(pacienteExistente);

        String motivoConsulta = pacienteConMotivoForm.getMotivoConsultaActual();
        StringBuilder detallesHistorial = new StringBuilder();
        boolean hayMotivo = motivoConsulta != null && !motivoConsulta.trim().isEmpty();

        if (hayMotivo) {
            detallesHistorial.append("Motivo de Consulta/Actualización: \n").append(motivoConsulta.trim()).append("\n\n");
        } else {
            detallesHistorial.append("Actualización de datos del paciente (sin motivo de consulta específico ingresado para esta actualización).\n\n");
        }

        detallesHistorial.append("Datos del Paciente Registrados en esta Entrada:\n");
        detallesHistorial.append("- Nombre: ").append(pacienteGuardado.getNombre()).append("\n");
        detallesHistorial.append("- Edad: ").append(pacienteGuardado.getEdad() != null ? pacienteGuardado.getEdad() : "N/A").append("\n");
        detallesHistorial.append("- Sexo: ").append(pacienteGuardado.getSexo() != null ? pacienteGuardado.getSexo().getDescripcion() : "N/A").append("\n");
        detallesHistorial.append("- Identificación: ").append(pacienteGuardado.getIdentificacion() != null ? pacienteGuardado.getIdentificacion() : "N/A").append("\n");
        detallesHistorial.append("- Edad Estimada Lesión: ").append(pacienteGuardado.getEdadEstimadaLesion() != null ? pacienteGuardado.getEdadEstimadaLesion().getDescripcion() : "N/A").append("\n");
        detallesHistorial.append("- Área Corporal Afectada: ").append(pacienteGuardado.getAreaCorporalAfectadaPredominante() != null ? pacienteGuardado.getAreaCorporalAfectadaPredominante().getDescripcion() : "N/A").append("\n");
        detallesHistorial.append("- Tipo Piel Fitzpatrick: ").append(pacienteGuardado.getTipoPielFitzpatrick() != null ? pacienteGuardado.getTipoPielFitzpatrick().getDescripcion() : "N/A").append("\n");
        detallesHistorial.append("- Tamaño General Lesión: ").append(pacienteGuardado.getTamanodeLesionGeneral() != null ? pacienteGuardado.getTamanodeLesionGeneral().getDescripcion() : "N/A").append("\n");
        detallesHistorial.append("- Antecedentes Cáncer Piel: ").append(pacienteGuardado.getAntecedentesFamiliaresCancer() != null ? pacienteGuardado.getAntecedentesFamiliaresCancer().getDescripcion() : "N/A").append("\n");

        EntradaHistorial nuevaEntrada = new EntradaHistorial(
                pacienteGuardado,
                hayMotivo ? "Actualización de Datos y Consulta" : "Actualización de Datos de Paciente",
                detallesHistorial.toString(),
                "Registro completado"
        );
        entradaHistorialRepositorio.save(nuevaEntrada);
        logger.info("Nueva entrada de historial creada para paciente ID {}", pacienteGuardado.getId());

        return pacienteGuardado;
    }
}