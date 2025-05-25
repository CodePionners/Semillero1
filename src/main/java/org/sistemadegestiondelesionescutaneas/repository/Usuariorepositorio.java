package org.sistemadegestiondelesionescutaneas.repository;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Usuariorepositorio extends JpaRepository<Usuario, Long> {

    /**
     * Encuentra un usuario por su nombre de usuario.
     * Carga de forma temprana el perfil del paciente asociado para evitar consultas N+1
     * si se accede al perfil inmediatamente después.
     * @param usuario El nombre de usuario a buscar.
     * @return El Usuario si se encuentra, o null en caso contrario.
     */
    @EntityGraph(attributePaths = { "perfilPaciente" })
    Usuario findByUsuario(String usuario); //

    Usuario findByEmail(String email); //
}
