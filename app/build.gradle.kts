plugins {
    // Aplica el plugin para crear aplicaciones Android
    id("com.android.application")
    // Aplica el plugin de Google Services para interactuar con Firebase
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics") // Plugin de Crashlytics
}

android {
    namespace = "com.example.applicate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.applicate"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    // Añade las dependencias específicas para Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Add the dependency for the Analytics library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.measurement.api)
    implementation(libs.firebase.database)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}