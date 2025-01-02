pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*") // Incluye paquetes relacionados con Android
                includeGroupByRegex("com\\.google.*") // Incluye paquetes de Google
                includeGroupByRegex("androidx.*")    // Incluye paquetes de AndroidX
            }
        }
        mavenCentral() // Repositorio para dependencias genéricas
        gradlePluginPortal() // Para plugins de Gradle
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Prioriza la configuración de repositorios en settings.gradle.kts
    repositories {
        google() // Repositorio oficial de Google
        mavenCentral() // Repositorio Maven Central
    }
}

// Nombre del proyecto raíz
rootProject.name = "APPLICATE"

// Incluye módulos del proyecto
include(":app")
