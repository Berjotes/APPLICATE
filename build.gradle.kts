plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.3.15" apply false // Última versión de Google Services
    id("com.google.firebase.crashlytics") version "2.9.6" apply false // Plugin de Crashlytics
}

subprojects {
    repositories {
        google() // Repositorio para las dependencias de Google
        mavenCentral() // Para otras dependencias
    }
}
