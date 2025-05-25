package org.sistemadegestiondelesionescutaneas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Importar las clases de autoconfiguración a excluir
import org.springframework.boot.actuate.autoconfigure.audit.AuditEventsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration; // Nueva importación
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication(exclude = {
        AuditEventsEndpointAutoConfiguration.class,       // Exclusión anterior
        BeansEndpointAutoConfiguration.class,           // Exclusión anterior
        CachesEndpointAutoConfiguration.class,          // Exclusión anterior
        HealthEndpointAutoConfiguration.class,          // Exclusión anterior
        InfoEndpointAutoConfiguration.class,            // Exclusión anterior
        ConditionsReportEndpointAutoConfiguration.class // Nueva exclusión añadida aquí
})
public class SalcApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalcApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}