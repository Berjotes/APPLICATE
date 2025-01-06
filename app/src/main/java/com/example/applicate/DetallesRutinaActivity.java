package com.example.applicate;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetallesRutinaActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvDetallesRutina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_rutina);

        db = FirebaseFirestore.getInstance();
        tvDetallesRutina = findViewById(R.id.tvDetallesRutina);

        String idCliente = getIntent().getStringExtra("idCliente");
        String idRutina = getIntent().getStringExtra("idRutina");

        Log.d("DetallesRutina", "ID Cliente: " + idCliente + ", ID Rutina: " + idRutina);

        if (idCliente != null && idRutina != null) {
            cargarDetallesRutina(idCliente, idRutina);
        } else {
            Toast.makeText(this, "Error al cargar los detalles de la rutina.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarDetallesRutina(String idCliente, String idRutina) {
        db.collection("CLIENTES").document(idCliente)
                .collection("RUTINAS").document(idRutina)
                .collection("EJERCICIOS_RUTINA")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("DetallesRutina", "No se encontraron ejercicios para la rutina.");
                        tvDetallesRutina.setText("No hay ejercicios en esta rutina.");
                        return;
                    }

                    Log.d("DetallesRutina", "Ejercicios encontrados: " + queryDocumentSnapshots.size());

                    Map<String, List<String>> ejerciciosAgrupados = new HashMap<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d("DetallesRutina", "Documento: " + document.getId() + " => " + document.getData());

                        String diaSemana = document.getString("dia_semana");
                        String nombreEjercicio = document.getString("nombre_ejercicio");
                        String numSeries = document.getString("num_series");
                        String numRepeticiones = document.getString("num_repeticiones");
                        String peso = document.getString("peso");
                        String descripcion = document.getString("descripcion");

                        if (diaSemana == null) diaSemana = "SIN DÍA";

                        String exerciseDetails = "- " + nombreEjercicio + " - " +
                                numSeries + " series - " +
                                numRepeticiones + " repeticiones - " +
                                peso + " kg - " +
                                descripcion;

                        ejerciciosAgrupados.putIfAbsent(diaSemana, new ArrayList<>());
                        ejerciciosAgrupados.get(diaSemana).add(exerciseDetails);
                    }

                    mostrarDetallesRutina(ejerciciosAgrupados);
                })
                .addOnFailureListener(e -> {
                    Log.e("DetallesRutina", "Error al cargar ejercicios", e);
                    FirebaseCrashlytics.getInstance().recordException(e); // Registra la excepción en Crashlytics
                    Toast.makeText(this, "Error al cargar los ejercicios de la rutina.", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDetallesRutina(Map<String, List<String>> ejerciciosAgrupados) {
        StringBuilder detalles = new StringBuilder();

        for (Map.Entry<String, List<String>> entry : ejerciciosAgrupados.entrySet()) {
            String diaSemana = entry.getKey();
            List<String> ejercicios = entry.getValue();

            detalles.append(diaSemana.toUpperCase()).append(":\n");

            for (String ejercicio : ejercicios) {
                detalles.append(ejercicio).append("\n");
            }

            detalles.append("\n");
        }

        Log.d("DetallesRutina", "Datos a mostrar:\n" + detalles);
        tvDetallesRutina.setText(detalles.toString());
    }

}