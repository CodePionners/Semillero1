package org.sistemadegestiondelesionescutaneas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.cache.annotation.EnableCaching;

// Asegúrate de que no haya importaciones no utilizadas aquí relacionadas con:
// jakarta.servlet.SessionTrackingMode
// org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
// org.springframework.boot.web.server.WebServerFactoryCustomizer
// java.util.Set
// jakarta.servlet.ServletContext
// jakarta.servlet.ServletException

@SpringBootApplication
@EnableCaching
public class SalcApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SalcApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { //
        return new BCryptPasswordEncoder();
    }

    // Cualquier bean WebServerFactoryCustomizer o ServletContextInitializer
    // que intentara configurar SessionTrackingModes en esta clase ha sido eliminado
    // para resolver el error de compilación.
    // El problema del jsessionid en la URL se maneja permitiendo punto y coma
    // en SecurityConfig.java (Opción B2).
}