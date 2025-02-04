package com.example.applicate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == R.id.cerrar_sesion) {
                Log.d("MainActivity", "Cerrar sesión");
                logout();
                return true;
            }
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e); // Registrar en Crashlytics
            Toast.makeText(this, "Ocurrió un error al procesar la acción.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut(); // Cerrar sesión en Firebase
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class); // Redirigir a Login
        startActivity(intent);
        finish(); // Finalizar la actividad actual
    }

}
