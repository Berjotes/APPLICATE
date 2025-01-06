package com.example.applicate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email, pass;
    private Button botonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.etEmail);
        pass = findViewById(R.id.etPassword);
        botonLogin = findViewById(R.id.btnLogin);

        botonLogin.setOnClickListener(v -> {
            String email = this.email.getText().toString().trim();
            String password = pass.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                iniciarSesion(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "Por favor, introduce tus credenciales", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarSesion(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login exitoso
                        FirebaseUser usuario = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Bienvenido " + usuario.getEmail(), Toast.LENGTH_SHORT).show();
                        irAListaClientes();
                    } else {
                        // Error en el login
                        Log.w("LoginActivity", "iniciarSesionConEmail:error", task.getException());

                        // Enviar detalles del error a Crashlytics
                        FirebaseCrashlytics.getInstance().recordException(task.getException());
                        Toast.makeText(LoginActivity.this, "Error: Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void irAListaClientes() {
        Intent intent = new Intent(LoginActivity.this, GestorClientesActivity.class);
        startActivity(intent);
        finish();
    }

}