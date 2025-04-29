plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    dependencies {
        // Dependencias que ya podrían estar aquí
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
        developmentOnly("org.springframework.boot:spring-boot-devtools")
        runtimeOnly("com.h2database:h2") // H2 suele ser runtimeOnly o implementation
        testImplementation("org.springframework.boot:spring-boot-starter-test")

        // ---> AÑADE ESTA LÍNEA <---
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")

        // Otras dependencias...
    }
}

tasks.test {
    useJUnitPlatform()

    plugins {
        id("org.springframework.boot") version "TU_VERSION_DE_SPRING_BOOT" // Asegúrate de usar la versión correcta
        id("io.spring.dependency-management") version "TU_VERSION_DE_DEPENDENCY_MANAGEMENT" // Este también es común con Spring Boot
        kotlin("jvm") version "TU_VERSION_DE_KOTLIN" // Si usas Kotlin
        kotlin("plugin.spring") version "TU_VERSION_DE_KOTLIN" // Si usas Kotlin y plugin de Spring
    }
}