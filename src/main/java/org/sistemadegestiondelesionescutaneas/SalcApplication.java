package org.sistemadegestiondelesionescutaneas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Considera añadir exclusiones específicas si la carga perezosa global no es suficiente
// import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
// import org.springframework.boot.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
// Ejemplo de exclusión de autoconfiguraciones específicas si es absolutamente necesario después de perfilar:
// @SpringBootApplication(exclude = {
//    DataSourceHealthContributorAutoConfiguration.class,
//    SpringApplicationAdminJmxAutoConfiguration.class // Si JMX no se utiliza
// })
public class SalcApplication {

    public static void main(String[] args) {
        // Puedes habilitar el analizador de inicio de Spring Boot para obtener información más detallada
        // SpringApplication application = new SpringApplication(SalcApplication.class);
        // application.setApplicationStartup(new BufferingApplicationStartup(2048));
        // application.run(args);
        SpringApplication.run(SalcApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}