package org.sistemadegestiondelesionescutaneas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.cache.annotation.EnableCaching;

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
}