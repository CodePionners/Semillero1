package org.sistemadegestiondelesionescutaneas.repository;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Usuariorepositorio extends JpaRepository<Usuario, Long> {
    Usuario findByUsuario(String usuario);
    Usuario findByEmail(String email);
}
