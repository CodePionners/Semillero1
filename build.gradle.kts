plugins {
    java
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm")
}

group = "org.sistemadegestiondelesionescutaneas"
version = "0.0.1-SNAPSHOT"

java {
    // Especifica la versión de Java que usas
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    runtimeOnly("com.h2database:h2")
    implementation("com.mysql:mysql-connector-j:8.3.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("org.sistemadegestiondelesionescutaneas.SalcApplication")
}
kotlin {
    jvmToolchain(21)
}