package org.sistemadegestiondelesionescutaneas.repository;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Usuariorepositorio extends JpaRepository<Usuario, String> {
    Usuario findByUsuario(String usuario); }
