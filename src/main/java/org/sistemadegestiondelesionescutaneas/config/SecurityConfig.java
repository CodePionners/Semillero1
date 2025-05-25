package org.sistemadegestiondelesionescutaneas.config;

import org.sistemadegestiondelesionescutaneas.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
// AntPathRequestMatcher ya no se importa explícitamente si no se usa su constructor
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Considera habilitar CSRF en producción con la configuración adecuada
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers( // Ya no se usa 'new AntPathRequestMatcher(...)'
                                "/", //
                                "/login", //
                                "/perform_login", //
                                "/registro", //
                                "/mostrar-registro", //
                                "/css/**", //
                                "/js/**", //
                                "/imagenes/view/**" //
                                // Añade aquí cualquier otra ruta pública
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") //
                        .requestMatchers("/medico/**").hasRole("MEDICO") //
                        .requestMatchers("/paciente/**").hasRole("PACIENTE") //
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") //
                        .loginProcessingUrl("/perform_login") //
                        .defaultSuccessUrl("/", true) //
                        .failureUrl("/login?error=true") //
                        .permitAll()
                )
                .logout(logout -> logout
                        // Para logoutRequestMatcher, si necesitas especificar el método HTTP (ej. POST),
                        // puedes usar `new AntPathRequestMatcher("/logout", "POST")` si es estrictamente necesario,
                        // o mejor aún, configurar logout para que use un endpoint POST y protegerlo con CSRF.
                        // Si es un GET simple (no recomendado para acciones que cambian estado):
                        .logoutUrl("/logout") // Manera simplificada si es GET, o usa `logoutRequestMatcher` como antes si es POST
                        // Si mantienes el logoutRequestMatcher para especificar método POST (recomendado con CSRF):
                        // .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST")) // Esto aún puede generar warning si AntPathRequestMatcher se elimina
                        // La alternativa moderna y más segura es asegurar que el logout sea un POST
                        // y que CSRF esté habilitado. Si CSRF está deshabilitado, logoutUrl es más simple.
                        .logoutSuccessUrl("/login?logout=true") //
                        .invalidateHttpSession(true) //
                        .deleteCookies("JSESSIONID") //
                )
                .userDetailsService(customUserDetailsService); // Asegúrate de que tu CustomUserDetailsService está configurado

        return http.build();
    }
}