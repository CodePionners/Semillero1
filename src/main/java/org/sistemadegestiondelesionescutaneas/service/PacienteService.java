package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Diagnostico;
import org.sistemadegestiondelesionescutaneas.model.EntradaHistorial;
import org.sistemadegestiondelesionescutaneas.model.Paciente;
import org.sistemadegestiondelesionescutaneas.model.TipoReporte;
import org.sistemadegestiondelesionescutaneas.repository.EntradaHistorialrepositorio;
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public Paciente updatePaciente(Paciente pacienteForm) {
        Paciente pacienteExistente = pacienteRepositorio.findById(pacienteForm.getId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + pacienteForm.getId()));

        pacienteExistente.setNombre(pacienteForm.getNombre());
        pacienteExistente.setEdad(pacienteForm.getEdad());
        pacienteExistente.setSexo(pacienteForm.getSexo());
        pacienteExistente.setTipoReporteActual(pacienteForm.getTipoReporteActual());
        pacienteExistente.setDiagnosticoPredominante(pacienteForm.getDiagnosticoPredominante());
        pacienteExistente.setEdadEstimadaLesion(pacienteForm.getEdadEstimadaLesion());
        pacienteExistente.setAreaCorporalAfectadaPredominante(pacienteForm.getAreaCorporalAfectadaPredominante());
        pacienteExistente.setTipoPielFitzpatrick(pacienteForm.getTipoPielFitzpatrick());
        pacienteExistente.setTamanodeLesionGeneral(pacienteForm.getTamanodeLesionGeneral());
        pacienteExistente.setAntecedentesFamiliaresCancer(pacienteForm.getAntecedentesFamiliaresCancer());

        Paciente pacienteGuardado = pacienteRepositorio.save(pacienteExistente);

        String motivoConsultaOAdicional = pacienteForm.getMotivoConsultaActual();
        TipoReporte tipoReporteParaHistorial = pacienteGuardado.getTipoReporteActual();
        Diagnostico diagnosticoParaHistorial = pacienteGuardado.getDiagnosticoPredominante();

        EntradaHistorial nuevaEntrada = new EntradaHistorial();
        nuevaEntrada.setPaciente(pacienteGuardado);
        nuevaEntrada.setFechaHora(LocalDateTime.now());

        if (tipoReporteParaHistorial != null) {
            nuevaEntrada.setEvento(tipoReporteParaHistorial.getDescripcion());
        } else {
            nuevaEntrada.setEvento("Actualización de Datos");
        }

        // Asignar el motivo de consulta (o notas) al campo 'detalles'
        // Ya no se añade la frase "Actualización de datos del paciente (sin motivo...)"
        nuevaEntrada.setDetalles(motivoConsultaOAdicional);

        nuevaEntrada.setTipoReporte(tipoReporteParaHistorial);
        nuevaEntrada.setDiagnostico(diagnosticoParaHistorial);

        if (diagnosticoParaHistorial != null) {
            nuevaEntrada.setEstadoOriginal(diagnosticoParaHistorial.getDescripcion());
        } else {
            nuevaEntrada.setEstadoOriginal("No especificado");
        }

        entradaHistorialRepositorio.save(nuevaEntrada);
        logger.info("Nueva entrada de historial creada para paciente ID {} con Evento: '{}', Tipo Reporte: {}, Diagnóstico: {}",
                pacienteGuardado.getId(),
                nuevaEntrada.getEvento(),
                tipoReporteParaHistorial,
                diagnosticoParaHistorial);

        return pacienteGuardado;


    }
}

