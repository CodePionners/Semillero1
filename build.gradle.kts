plugins {
    java // Plugin base de Java
    id("org.springframework.boot") version "3.2.5" // Versión estable de Spring Boot (ajusta si necesitas una más reciente como 3.3.x, pero 3.4.5 podría ser demasiado nueva o no existir)
    id("io.spring.dependency-management") version "1.1.4" // Plugin para gestionar versiones de dependencias
}

group = "org.sistemadegestiondelesionescutaneas" // Cambiado para ser más descriptivo
version = "0.0.1-SNAPSHOT" // Convención común para versiones iniciales

java {
    sourceCompatibility = JavaVersion.VERSION_21 // Especifica la versión de Java que usas
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral() // Repositorio principal para descargar dependencias
    // Si mavenCentral() tiene problemas, puedes añadir otros espejos o repositorios,
    // pero usualmente no es necesario.
    // maven { url = uri("https://repo.spring.io/milestone") }
    // maven { url = uri("https://repo.spring.io/snapshot") } // Para versiones milestone o snapshot
}

dependencies {
    // Spring Boot Starters (las versiones son gestionadas por el plugin de Spring Boot)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation") // Añadido porque aparecía en tus errores

    // Herramientas de desarrollo
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Base de Datos
    runtimeOnly("com.h2database:h2") // Base de datos en memoria para desarrollo/pruebas (si la usas)
    implementation("com.mysql:mysql-connector-j:8.3.0")  // Conector MySQL, asegúrate que esta versión exista y sea la que quieres. La 8.0.33 es muy estable también.

    // Pruebas
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test") // Para probar la seguridad
}

tasks.withType<Test> {
    useJUnitPlatform() // Asegura que se use JUnit 5 para las pruebas
}

// Configuración para que el plugin de Spring Boot sepa cuál es tu clase principal
// Reemplaza 'org.sistemadegestiondelesionescutaneas.SalcApplication' con la ruta completa a tu clase principal
// si es diferente.
springBoot {
    mainClass.set("org.sistemadegestiondelesionescutaneas.SalcApplication")
}
