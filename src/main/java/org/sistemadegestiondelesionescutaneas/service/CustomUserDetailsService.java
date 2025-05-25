package org.sistemadegestiondelesionescutaneas.service;

import org.sistemadegestiondelesionescutaneas.model.Usuario;
import org.sistemadegestiondelesionescutaneas.repository.Usuariorepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collection;
import java.util.Collections;

@Service
@Lazy
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private Usuariorepositorio usuarioRepositorio;

    @Override
    @Cacheable("userDetails") // Nombre de la caché para los detalles del usuario
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByUsuario(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                usuario.getUsuario(),
                usuario.getContrasena(), // Esta es la contraseña hasheada desde la BD
                getAuthorities(usuario)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        String rol = usuario.getRol();
        if (rol == null) {
            // Si el rol es nulo, devuelve una lista vacía de autoridades para evitar NullPointerException.
            // Considera si un usuario SIEMPRE debe tener un rol; si es así, esto podría indicar un problema de datos.
            return Collections.emptyList();
        }
        // Spring Security espera que los roles tengan el prefijo "ROLE_"
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));
    }
}