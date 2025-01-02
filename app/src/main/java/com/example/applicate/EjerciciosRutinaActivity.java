package com.example.applicate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EjerciciosRutinaActivity extends AppCompatActivity {

    private RecyclerView recyclerViewEjercicios;
    private Button btnAgregarEjercicios;
    private List<String> ejercicios = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicios_rutina);

        // Configurar Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Ejercicios en la Rutina");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewEjercicios = findViewById(R.id.recyclerViewEjercicios);
        recyclerViewEjercicios.setLayoutManager(new LinearLayoutManager(this));

        btnAgregarEjercicios = findViewById(R.id.btnAgregarEjercicio);
        btnAgregarEjercicios.setOnClickListener(v -> {
            // Lógica para agregar una rutina
            obtenerGruposMusculares();
        });
    }

    // Acción al pulsar el botón "Volver" en el toolbar
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Volver a la actividad de lista de clientes
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void obtenerGruposMusculares() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("EJERCICIOS") // Aquí aseguramos el nombre exacto de la colección
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No hay ejercicios disponibles.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Extraer los valores únicos de 'grupo_muscular'
                    Set<String> gruposMusculares = new HashSet<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String grupo = document.getString("grupo_muscular");
                        if (grupo != null) {
                            gruposMusculares.add(grupo);
                        }
                    }

                    if (gruposMusculares.isEmpty()) {
                        Toast.makeText(this, "No hay grupos musculares disponibles.", Toast.LENGTH_SHORT).show();
                    } else {
                        mostrarDialogoGruposMusculares(new ArrayList<>(gruposMusculares));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar los datos.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void mostrarDialogoGruposMusculares(List<String> gruposMusculares) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione un grupo muscular");

        String[] arrayGrupos = gruposMusculares.toArray(new String[0]);
        builder.setItems(arrayGrupos, (dialog, which) -> {
            String grupoSeleccionado = arrayGrupos[which];
            Toast.makeText(this, "Seleccionado: " + grupoSeleccionado, Toast.LENGTH_SHORT).show();
            // Aquí puedes cargar ejercicios de este grupo muscular
            obtenerEjerciciosPorGrupo(grupoSeleccionado);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void obtenerEjerciciosPorGrupo(String grupoMuscular) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("EJERCICIOS")
                .whereEqualTo("grupo_muscular", grupoMuscular)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> ejercicios = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombreEjercicio = document.getString("nombre_ejercicio");
                        if (nombreEjercicio != null) {
                            ejercicios.add(nombreEjercicio);
                        }
                    }
                    // Ordenar manualmente por nombre
                    Collections.sort(ejercicios);
                    mostrarDialogoEjercicios(grupoMuscular, ejercicios);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar ejercicios", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDialogoEjercicios(String grupoMuscular, List<String> ejercicios) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ejercicios de " + grupoMuscular);

        String[] arrayEjercicios = ejercicios.toArray(new String[0]);
        builder.setItems(arrayEjercicios, (dialog, which) -> {
            String ejercicioSeleccionado = arrayEjercicios[which];
            agregarEjercicioARutina(ejercicioSeleccionado);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void agregarEjercicioARutina(String ejercicio) {
        // Lógica para añadir el ejercicio a la rutina (por ejemplo, actualizar la base de datos o una lista local)
        Toast.makeText(this, ejercicio + " añadido a la rutina.", Toast.LENGTH_SHORT).show();
    }

}
