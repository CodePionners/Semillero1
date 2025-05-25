// settings.gradle.kts

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            // Esta estrategia puede ayudar a forzar versiones si hay conflictos,
            // pero la definición principal está en el bloque plugins de abajo.
            if (requested.id.namespace == "org.jetbrains.kotlin" || requested.id.id.startsWith("kotlin-")) {
                // Si necesitas forzar una versión aquí, podrías hacerlo,
                // pero idealmente, el bloque 'plugins' es suficiente.
            }
        }
    }
    plugins {
        // Define la versión para los plugins de Kotlin aquí
        kotlin("jvm") version "2.1.21"
        kotlin("plugin.spring") version "2.1.21"
        // Si usas otros plugins de Kotlin (ej. "kotlin-jpa"), añádelos aquí con la misma versión
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Semillero"