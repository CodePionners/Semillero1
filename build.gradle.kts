import org.jetbrains.kotlin.gradle.tasks.KotlinCompile // Para configurar opciones de compilación de Kotlin

// Define la versión de Kotlin explícitamente para usar en 'extra.properties'
// y potencialmente en la declaración de dependencias.
val kotlinVersion = "2.1.21" //
extra["kotlin.version"] = kotlinVersion // Ayuda a Spring Boot a alinear versiones de dependencias de Kotlin.
plugins {
    // 1. Aplicar los plugins de Kotlin POR ID SOLAMENTE.
// Las versiones se gestionan centralmente en settings.gradle.kts -> pluginManagement.
    kotlin("jvm") // NO se especifica 'version' aquí
    kotlin("plugin.spring") // NO se especifica 'version' aquí

    // 2. Aplicar el plugin de Java
    java //

    // 3. Aplicar plugins de Spring Boot y manejo de dependencias
    id("org.springframework.boot") version "3.5.0" //
    id("io.spring.dependency-management") version "1.1.7" //
}

group = "org.sistemadegestiondelesionescutaneas"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21 //
    targetCompatibility = JavaVersion.VERSION_21 //
}

repositories {
    mavenCentral()
}

dependencies {
    // Excluir Tomcat de spring-boot-starter-web
    implementation("org.springframework.boot:spring-boot-starter-web") { //
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat") //
    }
    // Añadir Undertow
    implementation("org.springframework.boot:spring-boot-starter-undertow") //

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf") //
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6") // <--- AÑADIDO PARA LA INTEGRACIÓN DE SPRING SECURITY CON THYMELEAF
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") //
    implementation("org.springframework.boot:spring-boot-starter-security") //
    implementation("org.springframework.boot:spring-boot-starter-validation") //
    implementation("org.springframework.boot:spring-boot-starter-actuator") //

    implementation("javax.cache:cache-api:1.1.1") //
    implementation("org.ehcache:ehcache:3.10.8") //
    implementation("org.hibernate.orm:hibernate-jcache:6.6.15.Final") //

    developmentOnly("org.springframework.boot:spring-boot-devtools") //

    runtimeOnly("com.h2database:h2") //
    implementation("com.mysql:mysql-connector-j:8.3.0") //

// CORREGIDO: La biblioteca estándar de Kotlin.
// La función kotlin("stdlib-jdk8") devuelve el descriptor de la dependencia.
// Debe ser envuelta by una configuración como 'implementation'.
    implementation(kotlin("stdlib-jdk8")) //
    // Alternativamente, si lo anterior sigue dando problemas o para ser más explícito:
    // implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    // o simplemente:
    // implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // La versión será gestionada por el plugin de Kotlin o Spring

    testImplementation(platform("org.junit:junit-bom:5.10.0")) //
    testImplementation("org.junit.jupiter:junit-jupiter") //
    testImplementation("org.springframework.boot:spring-boot-starter-test") //
    testImplementation("org.springframework.security:spring-security-test") //
}

tasks.withType<Test> {
    useJUnitPlatform() //
}

springBoot {
    mainClass.set("org.sistemadegestiondelesionescutaneas.SalcApplication") //
}

kotlin {
    jvmToolchain(21) //
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "21" //
    }
}