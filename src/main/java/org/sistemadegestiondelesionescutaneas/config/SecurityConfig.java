package org.sistemadegestiondelesionescutaneas.config;

import org.sistemadegestiondelesionescutaneas.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// BCryptPasswordEncoder se obtiene del bean definido en SalcApplication
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/",
                                "/login",
                                "/perform_login",
                                "/registro",
                                "/css/**",
                                "/js/**",
                                "/imagenes/view/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/medico/**").hasRole("MEDICO")
                        // Las siguientes reglas específicas deben ir ANTES de reglas más generales como .anyRequest().authenticated()
                        .requestMatchers("/imagenes/upload").hasAnyRole("PACIENTE", "ADMIN")
                        .requestMatchers("/imagenes/historial").hasAnyRole("PACIENTE", "ADMIN") // Si solo pacientes y admin pueden ver su propio historial
                        // Considera si un médico debería poder ver el historial de *sus* pacientes, lo cual requeriría una lógica más compleja
                        .requestMatchers("/imagenes/delete/**").hasAnyRole("PACIENTE", "MEDICO", "ADMIN") // O lógica más fina
                        // .requestMatchers("/paciente/**").hasRole("PACIENTE") // Esta podría ser demasiado general si las de arriba ya cubren /imagenes/*
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
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }
}