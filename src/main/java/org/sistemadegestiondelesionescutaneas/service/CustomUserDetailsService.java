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
// import org.springframework.cache.annotation.Cacheable; // Temporarily disabled for debugging login issues
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

@Service
@Lazy
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private Usuariorepositorio usuarioRepositorio;

    @Override
    // @Cacheable("userDetails") // Temporarily disabled to diagnose login issues. Re-enable with caution if login issues are resolved.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);
        Usuario usuario = usuarioRepositorio.findByUsuario(username);

        if (usuario == null) {
            logger.warn("User '{}' not found in repository.", username);
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        logger.info("User '{}' found. Role: {}", usuario.getUsuario(), usuario.getRol());
        if (usuario.getContrasena() == null || usuario.getContrasena().isEmpty()) {
            logger.error("CRITICAL: Password for user '{}' is NULL or EMPTY when fetched from repository.", username);
            // This would lead to "Empty encoded password" warning by BCryptPasswordEncoder
            // and subsequent authentication failure.
            // Throwing an exception here provides clearer feedback.
            throw new UsernameNotFoundException("Contraseña no configurada para el usuario: " + username);
        } else {
            logger.debug("Password present for user '{}'. Length: {}", username, usuario.getContrasena().length());
        }
        // Usuario.toString() is safe as it logs password presence/length, not the hash.
        logger.debug("Usuario object before creating UserDetails: {}", usuario.toString());

        return new org.springframework.security.core.userdetails.User(
                usuario.getUsuario(),
                usuario.getContrasena(), // This is the hashed password from the DB
                getAuthorities(usuario)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        String rol = usuario.getRol();
        if (rol == null || rol.trim().isEmpty()) {
            logger.warn("User '{}' has a null or empty role. Assigning no authorities.", usuario.getUsuario());
            return Collections.emptyList();
        }
        // Spring Security expects roles to have the "ROLE_" prefix.
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase());
        logger.info("Assigning authority '{}' to user '{}'", authority.getAuthority(), usuario.getUsuario());
        return Collections.singletonList(authority);
    }
}