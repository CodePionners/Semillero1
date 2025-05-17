plugins {
    id("java") // El plugin base de Java
    // ¡Asegúrate de que los plugins de Spring Boot y Dependency Management estén aquí!
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.4"
    // Si usas Kotlin, también deben estar aquí los plugins de Kotlin:
    // kotlin("jvm") version "TU_VERSION_DE_KOTLIN"
    // kotlin("plugin.spring") version "TU_VERSION_DE_KOTLIN"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// ¡Solo debe haber un bloque de dependencies!
dependencies {
    // Dependencias de prueba de JUnit
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Dependencias de Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // ¡Ahora developmentOnly debería ser reconocido porque el plugin está aplicado correctamente!
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Dependencias de base de datos (H2)
    runtimeOnly("com.h2database:h2") // O implementation, según tu necesidad

    // Dependencias de prueba de Spring Boot
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Dependencia de Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Dependencia de Spring Security para hashear las contraseñas
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Dependencia conexion con base de datos mysql
    implementation("com.mysql:mysql-connector-j:8.3.0")  // O la versión más reciente
}


// Cualquier otra configuración de Gradle (tareas, etc.) iría después del bloque dependencies