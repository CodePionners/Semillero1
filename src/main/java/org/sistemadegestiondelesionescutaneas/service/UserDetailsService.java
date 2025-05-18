package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private Usuariorepositorio usuarioRepositorio;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByUsuario(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        // Aquí conviertes tu entidad Usuario a la interfaz UserDetails de Spring Security.
        // Debes proporcionar el nombre de usuario, la contraseña (ya hasheada en tu BD)
        // y las autoridades/roles.
        return new org.springframework.security.core.userdetails.User(
                usuario.getUsuario(),
                usuario.getContrasena(), // Contraseña ya hasheada
                getAuthorities(usuario)   // Método para obtener los roles/autoridades
        );
    }

    // Método para obtener las autoridades/roles del usuario.
    // Spring Security espera que los roles tengan el prefijo "ROLE_".
    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        String rol = usuario.getRol();
        if (rol == null) {
            return Collections.emptyList();
        }
        // Asegúrate de que el rol almacenado en la BD ("PACIENTE", "MEDICO")
        // se convierta a "ROLE_PACIENTE", "ROLE_MEDICO".
        // Si no almacenas el prefijo "ROLE_" en la BD, añádelo aquí.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
        // Si un usuario pudiera tener múltiples roles separados por comas, por ejemplo:
        // return Arrays.stream(usuario.getRoles().split(","))
        //              .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()))
        //              .collect(Collectors.toList());
    }
}
