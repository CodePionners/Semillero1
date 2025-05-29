package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.model.*; // Importar todos los modelos necesarios (Enums)
import org.sistemadegestiondelesionescutaneas.repository.AnalisisDermatologicorepositorio; // Repositorio de Análisis
import org.sistemadegestiondelesionescutaneas.repository.ImagenLesionrepositorio;
import org.sistemadegestiondelesionescutaneas.repository.Pacienterepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Para el futuro guardado
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Para mensajes post-redirect
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    // (Métodos loginPage y homePage se mantienen igual)
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("loginError", "Usuario o contraseña incorrectos.");
        if (logout != null) model.addAttribute("logoutMessage", "Has cerrado sesión exitosamente.");
        return "login";
    }

    @GetMapping("/")
    public String homePage(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestURI = request.getRequestURI();
        model.addAttribute("requestURI", requestURI);
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority();
                if ("ROLE_PACIENTE".equals(role)) return "redirect:/imagenes/historial";
                if ("ROLE_MEDICO".equals(role)) return "forward:/medico/dashboard";
                if ("ROLE_ADMIN".equals(role)) { model.addAttribute("requestURI", "/admin/dashboard"); return "dashboard-admin"; }
            }
            return "redirect:/login?error=unauthorized_role";
        }
        return "redirect:/login";
    }

    @Controller
    public static class MedicoController {
        private static final Logger medicoLogger = LoggerFactory.getLogger(MedicoController.class);

        @Autowired
        private Pacienterepositorio pacienteRepositorio;
        @Autowired
        private ImagenLesionrepositorio imagenLesionRepositorio;
        @Autowired
        private AnalisisDermatologicorepositorio analisisDermatologicoRepositorio; // Inyectar

        // (Otros métodos del MedicoController como medicoDashboard, medicoGaleriaVerImagenes, etc., se mantienen igual)
        @GetMapping("/medico/dashboard")
        public String medicoDashboard(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "dashboard-medico";
        }

        @GetMapping("/medico/galeria/ver-imagenes")
        public String medicoGaleriaVerImagenes(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            try {
                List<ImagenLesion> imagenes = imagenLesionRepositorio.findAllWithPacienteOrderByFechaSubidaDesc();
                model.addAttribute("imagenesPrincipales", imagenes);
            } catch (Exception e) {
                medicoLogger.error("Error al cargar imágenes para la galería: {}", e.getMessage(), e);
                model.addAttribute("imagenesPrincipales", Collections.emptyList());
                model.addAttribute("errorMessageGalería", "No se pudieron cargar las imágenes.");
            }
            return "medico-galeria-principal";
        }

        @GetMapping("/medico/imagenes/cargar-para-paciente")
        public String medicoCargarImagen(Model model, HttpServletRequest request, Authentication authentication) {
            model.addAttribute("requestURI", request.getRequestURI());
            model.addAttribute("dashboardReturnUrl", "/medico/dashboard");
            String userRole = "";
            if (authentication != null && authentication.isAuthenticated() &&
                    authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO"))) {
                userRole = "MEDICO";
            }
            model.addAttribute("userRole", userRole);
            return "cargar-imagen";
        }

        @GetMapping("/medico/pacientes/lista")
        public String medicoPacientesLista(
                @RequestParam(name = "identificacionBusqueda", required = false) String identificacionBusqueda,
                Model model, HttpServletRequest request) {

            String requestURI = request.getRequestURI();
            medicoLogger.info("Accediendo a Medico Pacientes - Lista Análisis (URI: {}). Búsqueda por ID: {}", requestURI, identificacionBusqueda);
            model.addAttribute("requestURI", requestURI);

            // Cargar valores para los desplegables
            model.addAttribute("listaSexo", Sexo.values());
            model.addAttribute("listaEdadEstimada", EdadEstimada.values());
            model.addAttribute("listaAreaCorporalAfectada", AreaCorporalAfectada.values());
            model.addAttribute("listaTipoPielFitzpatrick", TipoPielFitzpatrick.values());
            model.addAttribute("listaTamanodeLesion", TamanodeLesion.values());
            model.addAttribute("listaAntecedentesFamiliaresCancer", AntecedentesFamiliaresCancer.values());
            // Podrías añadir Diagnostico.values() si también quieres un desplegable para el diagnóstico

            if (identificacionBusqueda != null && !identificacionBusqueda.trim().isEmpty()) {
                Optional<Paciente> pacienteOpt = pacienteRepositorio.findByIdentificacion(identificacionBusqueda.trim());
                if (pacienteOpt.isPresent()) {
                    Paciente pacienteEncontrado = pacienteOpt.get();
                    model.addAttribute("pacienteEncontrado", pacienteEncontrado);
                    medicoLogger.info("Paciente encontrado: {} {}", pacienteEncontrado.getNombre(), pacienteEncontrado.getIdentificacion());

                    // Buscar el análisis más reciente o crear uno nuevo si no existe
                    Optional<AnalisisDermatologico> analisisOpt = analisisDermatologicoRepositorio.findTopByPacienteOrderByFechahoraanalisisDesc(pacienteEncontrado);

                    AnalisisDermatologico analisisParaForm;
                    if (analisisOpt.isPresent()) {
                        analisisParaForm = analisisOpt.get();
                        medicoLogger.info("Análisis existente encontrado para paciente ID {}: Análisis ID {}", pacienteEncontrado.getId(), analisisParaForm.getId());
                    } else {
                        analisisParaForm = new AnalisisDermatologico();
                        analisisParaForm.setPaciente(pacienteEncontrado); // Asocia el paciente nuevo
                        // Establecer valores por defecto si es un nuevo análisis
                        analisisParaForm.setFechahoraanalisis(LocalDateTime.now());
                        analisisParaForm.setSexo(pacienteEncontrado.getSexo() != null ? pacienteEncontrado.getSexo() : Sexo.OTRO); // Tomar del paciente si existe
                        analisisParaForm.setDiagnostico(Diagnostico.INDETERMINADO); // Un valor por defecto
                        // Otros valores por defecto
                        analisisParaForm.setEdadestimada(EdadEstimada.DESCONOCIDA);
                        analisisParaForm.setAreacorporalafectada(AreaCorporalAfectada.NO_ESPECIFICADA);
                        analisisParaForm.setTipopielfitzpatrick(TipoPielFitzpatrick.NO_ESPECIFICADO);
                        analisisParaForm.setTamanodelesion(TamanodeLesion.NO_MEDIDO);
                        analisisParaForm.setAntecedentesfamiliarescancer(AntecedentesFamiliaresCancer.NO_ESPECIFICADO);
                        analisisParaForm.setHistoriallesionesprevias(false);
                        medicoLogger.info("No se encontró análisis existente para paciente ID {}. Creando uno nuevo para el formulario.", pacienteEncontrado.getId());
                    }
                    model.addAttribute("analisisForm", analisisParaForm); // Para el th:object del form

                } else {
                    medicoLogger.warn("Paciente con identificación '{}' no encontrado.", identificacionBusqueda);
                    model.addAttribute("pacienteNoEncontradoError", "Paciente con identificación '" + identificacionBusqueda + "' no encontrado.");
                    model.addAttribute("analisisForm", new AnalisisDermatologico()); // Formulario vacío
                }
            } else {
                model.addAttribute("analisisForm", new AnalisisDermatologico()); // Formulario vacío si no hay búsqueda
            }

            // La lista general de pacientes ya no es el foco principal de esta vista según la nueva solicitud.
            // Se podría mantener si se quiere mostrar debajo del formulario de búsqueda/edición.
            // Por ahora, la lógica de carga de 'todosLosPacientes' se omite para centrarse en la búsqueda individual.
            // Si aún se necesita, se puede reincorporar:
            // List<Paciente> todosLosPacientes = pacienteRepositorio.findAllWithAnalisisAndDiagnostico(Sort.by(Sort.Direction.ASC, "p.nombre"));
            // model.addAttribute("pacientes", todosLosPacientes);

            return "medico-pacientes-lista"; // Nueva plantilla o la modificada "medico-pacientes-lista"
        }

        // POST handler para guardar los cambios del análisis (EJEMPLO BÁSICO)
        @PostMapping("/medico/pacientes/guardar-analisis")
        public String guardarAnalisisDermatologico(
                AnalisisDermatologico analisisForm, // Spring poblará esto con los datos del form
                @RequestParam("pacienteIdHidden") Long pacienteId, // ID del paciente para asociar/confirmar
                RedirectAttributes redirectAttributes) {

            medicoLogger.info("Intentando guardar análisis ID: {} para paciente ID: {}", analisisForm.getId(), pacienteId);

            try {
                Paciente paciente = pacienteRepositorio.findById(pacienteId)
                        .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + pacienteId));

                analisisForm.setPaciente(paciente); // Asegurar la asociación correcta

                // Si es un análisis nuevo (ID es null) y no tiene imagen, no se puede guardar aún
                // Esta lógica asume que un análisis siempre está ligado a una imagen.
                // Si se permite crear análisis sin imagen inicialmente, esta lógica debe cambiar.
                if (analisisForm.getId() == null && analisisForm.getImagen() == null) {
                    // Para crear un nuevo análisis desde aquí, necesitaríamos un flujo para seleccionar/subir imagen primero
                    // o permitir análisis sin imagen. Por ahora, asumimos que se edita uno existente
                    // o que la creación de uno nuevo se maneja de otra forma (ej. al subir una imagen).
                    medicoLogger.warn("Intento de guardar nuevo análisis sin imagen asociada para paciente ID {}", pacienteId);
                    redirectAttributes.addFlashAttribute("errorMessageAnalisis", "No se puede crear un nuevo análisis directamente aquí sin una imagen asociada. Este formulario es principalmente para editar.");
                    return "redirect:/medico/pacientes/lista?identificacionBusqueda=" + paciente.getIdentificacion();
                }

                // Si el ID no es null, es una actualización. Si es null, es una inserción.
                // JPA save maneja ambos casos.
                analisisForm.setFechahoraanalisis(LocalDateTime.now()); // Actualizar fecha en cada guardado/modificación
                analisisDermatologicoRepositorio.save(analisisForm);

                redirectAttributes.addFlashAttribute("successMessageAnalisis", "Datos del análisis guardados exitosamente para el paciente " + paciente.getNombre() + ".");
                medicoLogger.info("Análisis ID {} guardado/actualizado para paciente ID {}", analisisForm.getId(), pacienteId);
                return "redirect:/medico/pacientes/lista?identificacionBusqueda=" + paciente.getIdentificacion();

            } catch (Exception e) {
                medicoLogger.error("Error al guardar el análisis dermatológico: {}", e.getMessage(), e);
                redirectAttributes.addFlashAttribute("errorMessageAnalisis", "Error al guardar los datos del análisis: " + e.getMessage());
                // Obtener la identificación del paciente si es posible para la redirección
                String identificacion = "";
                if (analisisForm.getPaciente() != null && analisisForm.getPaciente().getIdentificacion() != null) {
                    identificacion = analisisForm.getPaciente().getIdentificacion();
                } else if (pacienteId != null) {
                    pacienteRepositorio.findById(pacienteId).ifPresent(p -> {
                        // No se puede asignar a variable externa desde lambda directamente así.
                    });
                    // Para obtener la identificación si solo tenemos pacienteId, necesitaríamos otra consulta.
                    // Es mejor asegurar que el paciente se cargue y su identificación esté disponible.
                    // O redirigir a la página de búsqueda general.
                }
                return "redirect:/medico/pacientes/lista" + (identificacion.isEmpty() ? "" : "?identificacionBusqueda=" + identificacion);
            }
        }


        // (Otros métodos como agregar paciente, reportes, historial se mantienen)
        @GetMapping("/medico/pacientes/agregar")
        public String medicoAgregarPacienteForm(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "medico-pacientes-lista"; // Temporalmente
        }
        @GetMapping("/medico/reportes/generar")
        public String medicoReportesGenerar(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "medico-reportes-generar";
        }
        @GetMapping("/medico/reportes/ver-generados")
        public String medicoReportesVerGenerados(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "forward:/medico/dashboard";
        }
        @GetMapping("/medico/historial/consultas")
        public String medicoHistorialConsultas(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
            return "medico-historial-consultas";
        }
    }
}