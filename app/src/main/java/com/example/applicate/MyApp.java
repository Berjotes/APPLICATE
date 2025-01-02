package com.example.applicate;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);

        // Aquí puedes inicializar otras librerías globales si es necesario.
    }
}
