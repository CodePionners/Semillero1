package org.sistemadegestiondelesionescutaneas.config;

import org.sistemadegestiondelesionescutaneas.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public HttpFirewall allowSemicolonHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        return firewall;
    }

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
                                "/images/**",
                                "/icons/**",
                                "/imagenes/view/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/medico/**").hasRole("MEDICO")
                        // NUEVAS REGLAS PARA PACIENTE
                        .requestMatchers("/paciente/historial", "/paciente/historial/descargar/**", "/paciente/imagenes/cargar").hasRole("PACIENTE")
                        // FIN NUEVAS REGLAS PARA PACIENTE
                        .requestMatchers("/imagenes/upload").hasAnyRole("PACIENTE", "MEDICO", "ADMIN")
                        .requestMatchers("/imagenes/historial").hasAnyRole("PACIENTE", "ADMIN") // Esta ruta ya no será la principal para paciente
                        .requestMatchers("/imagenes/delete/**").hasAnyRole("PACIENTE", "MEDICO", "ADMIN")
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
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }
}
