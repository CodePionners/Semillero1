package org.sistemadegestiondelesionescutaneas.controller;

import org.sistemadegestiondelesionescutaneas.service.Autenticacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class Controladorautenticacion {

    private final Autenticacion autenticacionService;

    @Autowired
    public Controladorautenticacion(Autenticacion autenticacionService) {
        this.autenticacionService = autenticacionService;
    }
}