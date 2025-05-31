package org.sistemadegestiondelesionescutaneas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/medico")
public class MedicoController {

    @GetMapping("/pacientes")
    public String mostrarPacientes() {
        return "dashboard-paciente"; // Este debe coincidir con el nombre del HTML en templates
    }
}