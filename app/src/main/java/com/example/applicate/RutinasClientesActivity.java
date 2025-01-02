package com.example.applicate;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicate.adaptadores.AdaptadorRutina;
import com.example.applicate.modelos.Rutina;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RutinasClientesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnAgregarRutina;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutinas_clientes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        String nombreCliente = getIntent().getStringExtra("nombreCliente");
        String idCliente = getIntent().getStringExtra("idCliente");

        if (nombreCliente != null && !nombreCliente.isEmpty()) {
            getSupportActionBar().setTitle(nombreCliente);
        } else if (idCliente != null) {
            // Si no se pasa el nombre, buscarlo en Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("CLIENTES").document(idCliente)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String nombreObtenido = documentSnapshot.getString("nombre");
                        if (nombreObtenido != null && !nombreObtenido.isEmpty()) {
                            getSupportActionBar().setTitle(nombreObtenido);
                        } else {
                            getSupportActionBar().setTitle("Rutinas");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RutinasClientesActivity", "Error al obtener el nombre del cliente", e);
                        getSupportActionBar().setTitle("Rutinas");
                    });
        } else {
            getSupportActionBar().setTitle("Rutinas");
        }

        recyclerView = findViewById(R.id.recyclerViewRutinas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        if (idCliente == null) {
            Toast.makeText(this, "Error: Cliente no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        cargarRutinasCliente(idCliente);

        btnAgregarRutina = findViewById(R.id.btnAgregarRutina);
        btnAgregarRutina.setOnClickListener(v -> {
            db.collection("CLIENTES").document(idCliente).collection("RUTINAS")
                    .orderBy("num_rutina")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int siguienteNumRutina = 1;
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Long maxNumRutina = queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() - 1)
                                    .getLong("num_rutina");
                            if (maxNumRutina != null) {
                                siguienteNumRutina = maxNumRutina.intValue() + 1;
                            }
                        }

                        Intent intent = new Intent(RutinasClientesActivity.this, AgregarEjercicioActivity.class);
                        intent.putExtra("idCliente", idCliente);
                        intent.putExtra("numRutina", siguienteNumRutina);
                        intent.putExtra("nombreCliente", nombreCliente); // Pasar el nombre
                        intent.putExtra("estaEditando", false); // Modo creación
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al calcular el número de rutina.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        obtenerRutinas(); // Refresca la lista de rutinas al volver a la actividad
    }

    private void obtenerRutinas() {
        String idCliente = getIntent().getStringExtra("idCliente");
        if (idCliente == null || idCliente.isEmpty()) {
            Toast.makeText(this, "Error: Cliente no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("CLIENTES").document(idCliente).collection("RUTINAS")
                .orderBy("num_rutina") // Ordenar por el campo num_rutina
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Rutina> rutinas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long numRutina = document.getLong("num_rutina");
                        String fechaCreacion = document.getString("fecha_creacion");

                        if (numRutina != null && fechaCreacion != null) {
                            rutinas.add(new Rutina(numRutina.intValue(), fechaCreacion));
                        } else {
                            Log.w("RutinasClientes", "Documento sin campos requeridos: " + document.getId());
                        }
                    }

                    actualizarVistaRutinas(rutinas);
                })
                .addOnFailureListener(e -> {
                    Log.e("RutinasClientes", "Error al obtener rutinas", e);
                    Toast.makeText(this, "Error al cargar las rutinas", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rutinas, menu);
        return true;
    }

    // Accion al pulsar el botón "Volver" en el toolbar
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

    private void cargarRutinasCliente(String idCliente) {
        if (idCliente == null || idCliente.isEmpty()) {
            Toast.makeText(this, "Error: ID de cliente no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("CLIENTES").document(idCliente).collection("RUTINAS")
                .orderBy("num_rutina") // Ordena las rutinas por el campo num_rutina
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Rutina> rutinas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Long numRutina = document.getLong("num_rutina");
                            String fechaCreacion = document.getString("fecha_creacion");

                            if (numRutina != null && fechaCreacion != null) {
                                rutinas.add(new Rutina(numRutina.intValue(), fechaCreacion));
                            } else {
                                Log.w("RutinasClientes", "Documento sin campos requeridos: " + document.getId());
                            }
                        }
                        if (rutinas.isEmpty()) {
                            Toast.makeText(this, "Este cliente no tiene rutinas.", Toast.LENGTH_SHORT).show();
                        }
                        actualizarVistaRutinas(rutinas);
                    } else {
                        Log.e("RutinasClientes", "Error al obtener rutinas", task.getException());
                        Toast.makeText(this, "Error al cargar las rutinas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RutinasClientes", "Error al acceder a la subcoleccion RUTINAS", e);
                    Toast.makeText(this, "Error al cargar las rutinas del cliente", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarVistaRutinas(List<Rutina> rutinas) {
        AdaptadorRutina adaptadorRutina = new AdaptadorRutina(rutinas);

        adaptadorRutina.setOnItemClickListener(rutina -> {
            String idCliente = getIntent().getStringExtra("idCliente");

            // Obtener el ID real del documento en RUTINAS
            db.collection("CLIENTES").document(idCliente)
                    .collection("RUTINAS")
                    .whereEqualTo("num_rutina", rutina.getNumRutina()) // Buscar el documento correcto
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String idRutina = document.getId(); // ID real del documento
                            Intent intent = new Intent(this, MostrarRutinaActivity.class);
                            intent.putExtra("idCliente", idCliente);
                            intent.putExtra("idRutina", idRutina); // Pasar el ID correcto
                            startActivity(intent);
                            break; // Salir del bucle al encontrar el documento
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al obtener el ID de la rutina.", Toast.LENGTH_SHORT).show();
                        Log.e("RutinasClientes", "Error al obtener el ID de la rutina", e);
                    });
        });
        adaptadorRutina.setOnItemLongClickListener(rutina -> mostrarOpcionesRutina(rutina));

        recyclerView.setAdapter(adaptadorRutina);
    }


    private void mostrarOpcionesRutina(Rutina rutina) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de Rutina")
                .setItems(new String[]{"Editar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        editarRutina(rutina); // Llama a la funcionalidad de edición
                    } else if (which == 1) {
                        mostrarDialogoConfirmacionBorrado(rutina); // Muestra la confirmación al eliminar
                    }
                });
        builder.show();
    }



    private void editarRutina(Rutina rutina) {
        String idCliente = getIntent().getStringExtra("idCliente");

        Intent intent = new Intent(this, AgregarEjercicioActivity.class);
        intent.putExtra("idCliente", idCliente);
        intent.putExtra("numRutina", rutina.getNumRutina());
        intent.putExtra("estaEditando", true); // Indicamos que es edición
        startActivity(intent);
    }

    private void mostrarDialogoConfirmacionBorrado(Rutina rutina) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Rutina #" + rutina.getNumRutina());
        builder.setMessage("¿Estás seguro de que deseas eliminar esta rutina? Esta acción no se puede deshacer.");

        AlertDialog dialog = builder.setPositiveButton("Eliminar", (d, which) -> eliminarRutina(rutina))
                .setNegativeButton("Cancelar", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false); // Desactivar mayúsculas
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false); // Desactivar mayúsculas
        });

        dialog.show();
    }



    private void eliminarRutina(Rutina rutina) {
        String idCliente = getIntent().getStringExtra("idCliente");
        if (idCliente == null || rutina == null) {
            Toast.makeText(this, "Error al eliminar la rutina.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("CLIENTES").document(idCliente).collection("RUTINAS")
                .whereEqualTo("num_rutina", rutina.getNumRutina())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Rutina eliminada correctamente.", Toast.LENGTH_SHORT).show();
                                    obtenerRutinas(); // Actualizar la lista
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al eliminar la rutina.", Toast.LENGTH_SHORT).show();
                                    Log.e("RutinasClientes", "Error al eliminar la rutina", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar la rutina para eliminar.", Toast.LENGTH_SHORT).show();
                });
    }


}