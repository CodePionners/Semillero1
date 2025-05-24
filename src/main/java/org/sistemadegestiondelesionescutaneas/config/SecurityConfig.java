package org.sistemadegestiondelesionescutaneas.config;

import org.sistemadegestiondelesionescutaneas.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// BCryptPasswordEncoder no necesita ser inyectado aquí si ya es un Bean manejado por Spring.
// Spring Security lo usará automáticamente con CustomUserDetailsService.
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // No es necesario @Autowired private BCryptPasswordEncoder passwordEncoder; aquí
    // si no lo usas explícitamente en este método. Spring Security lo encontrará.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // BCryptPasswordEncoder removido de los parámetros
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/perform_login"),
                                new AntPathRequestMatcher("/registro"),
                                new AntPathRequestMatcher("/mostrar-registro"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/imagenes/view/**")
                                // Añade aquí cualquier otra ruta pública
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/medico/**")).hasRole("MEDICO")
                        .requestMatchers(new AntPathRequestMatcher("/paciente/**")).hasRole("PACIENTE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );
        // Spring Security usará automáticamente el bean CustomUserDetailsService y
        // el bean BCryptPasswordEncoder (definido en SalcApplication) para la autenticación.
        return http.build();
    }
}