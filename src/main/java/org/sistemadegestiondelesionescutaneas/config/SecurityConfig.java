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
    private CustomUserDetailsService customUserDetailsService; //

    @Bean
    public HttpFirewall allowSemicolonHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall(); //
        firewall.setAllowSemicolon(true); //
        return firewall;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Considera habilitar CSRF en producción con la configuración adecuada
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/", //
                                "/login", //
                                "/perform_login", //
                                "/registro", //
                                "/css/**", //
                                "/js/**", //
                                "/images/**", // Añadido para permitir acceso a imágenes estáticas si las tienes en /static/images
                                "/icons/**",  // Añadido para permitir acceso a íconos estáticos si los tienes en /static/icons
                                "/imagenes/view/**" //
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") //
                        .requestMatchers("/medico/**").hasRole("MEDICO") // Esta regla cubre /medico/imagenes/cargar-para-paciente
                        .requestMatchers("/paciente/**").hasRole("PACIENTE") // Añadido para rutas específicas de paciente si las tienes
                        .requestMatchers("/imagenes/upload").hasAnyRole("PACIENTE", "MEDICO", "ADMIN") // Permitir a médicos subir imágenes
                        .requestMatchers("/imagenes/historial").hasAnyRole("PACIENTE", "ADMIN", "MEDICO") //
                        .requestMatchers("/imagenes/delete/**").hasAnyRole("PACIENTE", "MEDICO", "ADMIN") //
                        .anyRequest().authenticated() //
                )
                .formLogin(form -> form
                        .loginPage("/login") //
                        .loginProcessingUrl("/perform_login") //
                        .defaultSuccessUrl("/", true) //
                        .failureUrl("/login?error=true") //
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") //
                        .logoutSuccessUrl("/login?logout=true") //
                        .invalidateHttpSession(true) //
                        .deleteCookies("JSESSIONID") //
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService); //

        return http.build();
    }
}