import org.jetbrains.kotlin.gradle.tasks.KotlinCompile // Para configurar opciones de compilación de Kotlin

// Define la versión de Kotlin explícitamente para usar en 'extra.properties'
// y potencialmente en la declaración de dependencias.
val kotlinVersion = "2.1.21" // Versión de Kotlin que especificaste
extra["kotlin.version"] = kotlinVersion // Ayuda a Spring Boot a alinear versiones de dependencias de Kotlin.

plugins {
    // 1. Aplicar los plugins de Kotlin POR ID SOLAMENTE.
    // Las versiones se gestionan centralmente en settings.gradle.kts -> pluginManagement o por la BOM de Spring Boot.
    kotlin("jvm")
    kotlin("plugin.spring")

    // 2. Aplicar el plugin de Java
    java

    // 3. Aplicar plugins de Spring Boot y manejo de dependencias
    id("org.springframework.boot") version "3.5.0" // Versión de Spring Boot que especificaste
    id("io.spring.dependency-management") version "1.1.7" // Versión de Dependency Management que especificaste
}

group = "org.sistemadegestiondelesionescutaneas"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21 // Versión de Java que especificaste
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Cache (si lo usas, si no, puedes quitar estas)
    implementation("javax.cache:cache-api:1.1.1")
    implementation("org.ehcache:ehcache:3.10.8")
    // Asegúrate de que la versión de hibernate-jcache sea compatible con tu versión de Hibernate (traída por Spring Data JPA)
    // Spring Boot 3.5.0 usa Hibernate 6.5.x. La versión 6.6.15.Final podría ser demasiado nueva o incompatible.
    // Es mejor dejar que la BOM de Spring Boot maneje la versión de Hibernate y sus módulos si es posible,
    // o buscar la versión de hibernate-jcache que corresponda a Hibernate 6.5.x.
    // Por ahora, mantengo la que especificaste, pero revisa si hay conflictos.
    implementation("org.hibernate.orm:hibernate-jcache:6.6.15.Final")


    // Kotlin Standard Library
    implementation(kotlin("stdlib-jdk8")) // Esto debería usar la kotlinVersion definida arriba.

    // Base de Datos
    runtimeOnly("com.h2database:h2") // Para desarrollo/pruebas
    implementation("com.mysql:mysql-connector-j:8.3.0") // Versión de MySQL Connector que especificaste

    // Para generación de PDF con iText 7
    implementation("com.itextpdf:itext7-core:7.2.5") // O la versión más reciente compatible
    implementation("com.itextpdf:html2pdf:4.0.5")   // O la versión más reciente compatible

    // Para generación de CSV con Apache Commons CSV
    implementation("org.apache.commons:commons-csv:1.10.0") // O la versión más reciente compatible

    // DevTools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0")) // JUnit BOM que especificaste
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("org.sistemadegestiondelesionescutaneas.SalcApplication")
}

kotlin {
    jvmToolchain(21) // Configuración de toolchain de Kotlin que especificaste
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "21" // JVM target para Kotlin que especificaste
        // freeCompilerArgs = listOf("-Xjsr305=strict") // Opcional
    }
}
