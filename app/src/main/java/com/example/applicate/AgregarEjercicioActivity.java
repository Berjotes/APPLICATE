package com.example.applicate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.applicate.modelos.Ejercicio;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AgregarEjercicioActivity extends AppCompatActivity {

    private boolean editando;
    private boolean tieneCambiosSinGuardar = false;
    private String idRutina;
    private FirebaseFirestore db;
    private Map<String, List<Ejercicio>> ejerciciosPorDia = new HashMap<>();
    private Map<String, Set<String>> ejerciciosPorDía = new HashMap<>();
    private final Map<String, Set<String>> gruposMuscularesPorDia = new HashMap<>();
    private static final List<String> DIAS_SEMANA = List.of(
            "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_ejercicio);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editando = getIntent().getBooleanExtra("estaEditando", false);

        // Obtener los parámetros del Intent
        boolean estaEditando = getIntent().getBooleanExtra("estaEditando", false);
        String idCliente = getIntent().getStringExtra("idCliente");
        int numRutina = getIntent().getIntExtra("numRutina", -1);

        // Configurar el título según el modo
        getSupportActionBar().setTitle(estaEditando ? "Editar Rutina #" + numRutina : "Nueva Rutina");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();

        Button btnAgregarEjercicio = findViewById(R.id.btnAgregarEjercicio);
        btnAgregarEjercicio.setOnClickListener(v -> mostrarDialogoDiaSemana());

        Button btnSaveRoutine = findViewById(R.id.btnGuardarRutina);
        btnSaveRoutine.setOnClickListener(v -> {
            if (ejerciciosPorDia.isEmpty()) {
                Toast.makeText(this, "No hay ejercicios en la rutina para guardar.", Toast.LENGTH_SHORT).show();
                return;
            }
            guardarRutinaEnFirestore();
        });

        // Si es edición, cargar la rutina existente
        if (estaEditando) {
            cargarRutina(idCliente, numRutina);
        }

        actualizarListaEjercicios();
    }

    @SuppressLint("SetTextI18n")
    private void actualizarListaEjercicios() {
        LinearLayout container = findViewById(R.id.ejercicioContainer);
        container.removeAllViews(); // Limpiar el contenedor antes de añadir ejercicios

        // Ordenar los ejercicios por día de la semana
        for (String day : DIAS_SEMANA) {
            if (!ejerciciosPorDia.containsKey(day)) continue;

            List<Ejercicio> ejercicios = ejerciciosPorDia.get(day);

            // Mostrar encabezado para el día con los grupos musculares
            String grupoMuscular = String.join(", ", ejerciciosPorDía.get(day));
            TextView encabezadoDia = new TextView(this);
            encabezadoDia.setText(day + " (" + grupoMuscular + ")");
            encabezadoDia.setTextSize(18);
            encabezadoDia.setPadding(0, 16, 0, 8);
            container.addView(encabezadoDia);

            // Agregar cada ejercicio del día
            for (Ejercicio ejercicio : ejercicios) {
                TextView vistaEjercicio = new TextView(this);
                vistaEjercicio.setText(String.format(
                        "%s:\n%s series - %s repeticiones - %s kg\n%s",
                        ejercicio.getNombre(),
                        ejercicio.getNum_series(),
                        ejercicio.getNum_repes(),
                        ejercicio.getPeso(),
                        ejercicio.getDescripcion()
                ));
                vistaEjercicio.setPadding(16, 8, 16, 8);
                vistaEjercicio.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

                // Configurar clic largo para editar/eliminar
                vistaEjercicio.setOnLongClickListener(v -> {
                    mostrarDialogoOpcionesEjercicio(day, ejercicio);
                    return true;
                });

                container.addView(vistaEjercicio);
            }
        }
    }

    private void cargarRutina(String idCliente, int numRutina) {
        db.collection("CLIENTES").document(idCliente)
                .collection("RUTINAS")
                .whereEqualTo("num_rutina", numRutina)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No se encontró la rutina.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    queryDocumentSnapshots.forEach(document -> {
                        idRutina = document.getId(); // Guardo el ID de la rutina para futuras actualizaciones
                        cargarEjercicios(idCliente, idRutina);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar la rutina.", Toast.LENGTH_SHORT).show();
                    Log.e("AgregarEjercicio", "Error al cargar la rutina", e);
                });
    }

    private void cargarEjercicios(String idCliente, String idRutina) {
        db.collection("CLIENTES").document(idCliente)
                .collection("RUTINAS").document(idRutina)
                .collection("EJERCICIOS_RUTINA")
                .orderBy("posicion") // Ordenar por posición
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String dia = document.getString("dia_semana");
                            String nombre = document.getString("nombre_ejercicio");
                            String grupoMuscular = document.getString("grupo_muscular");
                            String numSeries = document.getString("num_series");
                            String numReps = document.getString("num_repeticiones");
                            String peso = document.getString("peso");
                            String descripcion = document.getString("descripcion");
                            Long posicion = document.getLong("posicion");

                            if (dia == null || nombre == null || grupoMuscular == null ||
                                    numSeries == null || numReps == null || peso == null ||
                                    descripcion == null || posicion == null) {
                                Log.w("AgregarEjercicio", "Documento incompleto: " + document.getId());
                                continue;
                            }

                            int pos = posicion.intValue(); // Convertir posición
                            Ejercicio ejercicio = new Ejercicio(dia, grupoMuscular, nombre, numSeries, numReps, peso, descripcion);
                            ejercicio.setPosicion(pos); // Asignar posición
                            agregarEjerciciosALaLista(dia, grupoMuscular, ejercicio);
                        } catch (Exception ex) {
                            Log.e("AgregarEjercicio", "Error al procesar documento: " + document.getId(), ex);
                        }
                    }

                    actualizarListaEjercicios();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar ejercicios.", Toast.LENGTH_SHORT).show();
                    Log.e("AgregarEjercicio", "Error al cargar ejercicios. ", e);
                    FirebaseCrashlytics.getInstance().recordException(e); // Registro en Crashlytics
                });

    }

    private void obtenerGruposMusculares(String dia) {
        db.collection("EJERCICIOS")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> gruposMusculares = new HashSet<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String grupo = document.getString("grupo_muscular");
                        if (grupo != null && !grupo.isEmpty()) { // Validar que el grupo no esté vacío
                            gruposMusculares.add(grupo);
                        }
                    }

                    if (gruposMusculares.isEmpty()) {
                        Toast.makeText(this, "No hay grupos musculares disponibles.", Toast.LENGTH_SHORT).show();
                        Log.w("AgregarEjercicio", "No se encontraron grupos musculares en la base de datos.");
                    } else {
                        List<String> gruposMuscularesOrdenados = new ArrayList<>(gruposMusculares);
                        gruposMuscularesOrdenados.sort(String::compareToIgnoreCase); // Ordenar alfabéticamente
                        mostrarDialogoGruposMusculares(dia, gruposMuscularesOrdenados);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar los datos.", Toast.LENGTH_SHORT).show();
                    Log.e("AgregarEjercicio", "Error al obtener grupos musculares", e);
                });
    }


    private void obtenerEjerciciosPorGrupoMuscular(String diaSeleccionado, String grupoMuscular) {
        db.collection("EJERCICIOS")
                .whereEqualTo("grupo_muscular", grupoMuscular)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No hay ejercicios disponibles para este grupo muscular.", Toast.LENGTH_SHORT).show();
                        Log.w("AgregarEjercicio", "No se encontraron ejercicios para el grupo muscular: " + grupoMuscular);
                        return;
                    }

                    List<String> ejercicios = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombreEjercicio = document.getString("nombre_ejercicio");
                        if (nombreEjercicio != null && !nombreEjercicio.isEmpty()) { // Validar que el nombre no esté vacío
                            ejercicios.add(nombreEjercicio);
                        } else {
                            Log.w("AgregarEjercicio", "Ejercicio con nombre vacío o nulo en documento: " + document.getId());
                        }
                    }

                    if (ejercicios.isEmpty()) {
                        Toast.makeText(this, "No hay ejercicios válidos para este grupo muscular.", Toast.LENGTH_SHORT).show();
                        Log.w("AgregarEjercicio", "Ejercicios encontrados pero inválidos para el grupo muscular: " + grupoMuscular);
                        return;
                    }

                    ejercicios.sort(String::compareToIgnoreCase); // Ordenar alfabéticamente
                    mostrarDialogoEjercicios(diaSeleccionado, grupoMuscular, ejercicios);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar ejercicios.", Toast.LENGTH_SHORT).show();
                    Log.e("AgregarEjercicio", "Error al cargar ejercicios para el grupo muscular: " + grupoMuscular, e);
                });
    }

    private void obtenerGruposMuscularesParaEdicion(String dia, Ejercicio ejercicio, View dialogView) {
        db.collection("EJERCICIOS")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
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
                        mostrarDialogoGruposMuscularesEdicion(dia, ejercicio, new ArrayList<>(gruposMusculares), dialogView);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar los datos.", Toast.LENGTH_SHORT).show();
                });
    }

    private void obtenerEjerciciosPorGrupoParaEdicion(String dia, Ejercicio ejercicio, String grupoMuscular, View dialogView) {
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

                    ejercicios.sort(String::compareToIgnoreCase);
                    mostrarDialogoEjerciciosEdicion(dia, ejercicio, grupoMuscular, ejercicios, dialogView);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar ejercicios.", Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarRutinaEnFirestore() {
        String idCliente = getIntent().getStringExtra("idCliente");
        int numRutina = getIntent().getIntExtra("numRutina", -1);

        if (idCliente == null || numRutina == -1) {
            Toast.makeText(this, "Error al guardar rutina: datos incompletos.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> datosRutina = new HashMap<>();
        datosRutina.put("num_rutina", numRutina);
        datosRutina.put("fecha_creacion", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        if (editando && idRutina != null) {
            db.collection("CLIENTES").document(idCliente)
                    .collection("RUTINAS").document(idRutina)
                    .set(datosRutina)
                    .addOnSuccessListener(aVoid -> {
                        guardarEjercicios(idCliente, idRutina);
                        tieneCambiosSinGuardar = false; // Cambios guardados
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar la rutina.", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("CLIENTES").document(idCliente)
                    .collection("RUTINAS")
                    .add(datosRutina)
                    .addOnSuccessListener(documentReference -> {
                        guardarEjercicios(idCliente, documentReference.getId());
                        tieneCambiosSinGuardar = false; // Cambios guardados
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la rutina.", Toast.LENGTH_SHORT).show());
        }
    }

    private void guardarEjercicios(String idCliente, String idRutina) {
        if (editando) {
            // Eliminar los ejercicios existentes antes de guardar los nuevos
            db.collection("CLIENTES").document(idCliente)
                    .collection("RUTINAS").document(idRutina)
                    .collection("EJERCICIOS_RUTINA")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                        // Ahora guardar los ejercicios actualizados
                        agregarEjerciciosAFirestore(idCliente, idRutina);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al limpiar ejercicios anteriores.", Toast.LENGTH_SHORT).show());
        } else {
            // Guardar ejercicios directamente
            agregarEjerciciosAFirestore(idCliente, idRutina);
        }
    }

    private void agregarEjerciciosAFirestore(String idCliente, String idRutina) {
        int posicion = 0; // Posición global inicial

        for (Map.Entry<String, List<Ejercicio>> diaEjercicios : ejerciciosPorDia.entrySet()) {
            String dia = diaEjercicios.getKey();
            List<Ejercicio> ejercicios = diaEjercicios.getValue();

            for (Ejercicio ejercicio : ejercicios) {
                try {
                    // Crear el mapa de datos del ejercicio
                    Map<String, Object> datosEjercicio = Map.of(
                            "nombre_ejercicio", ejercicio.getNombre(),
                            "num_series", ejercicio.getNum_series(),
                            "num_repeticiones", ejercicio.getNum_repes(),
                            "peso", ejercicio.getPeso(),
                            "descripcion", ejercicio.getDescripcion(),
                            "dia_semana", dia,
                            "grupo_muscular", ejercicio.getGrupoMuscular(),
                            "posicion", posicion
                    );

                    Log.d("AgregarEjercicioActivity", "Guardando ejercicio: " + ejercicio.getNombre() + " en posición: " + posicion);

                    db.collection("CLIENTES").document(idCliente)
                            .collection("RUTINAS").document(idRutina)
                            .collection("EJERCICIOS_RUTINA")
                            .add(datosEjercicio)
                            .addOnSuccessListener(documentReference ->
                                    Log.d("AgregarEjercicioActivity", "Ejercicio guardado con ID: " + documentReference.getId()))
                            .addOnFailureListener(e -> {
                                Log.e("AgregarEjercicioActivity", "Error al guardar ejercicio: " + ejercicio.getNombre(), e);
                                FirebaseCrashlytics.getInstance().recordException(e); // Registro en Crashlytics
                            });

                    posicion++; // Incrementar posición
                } catch (Exception ex) {
                    Log.e("AgregarEjercicioActivity", "Error al preparar datos del ejercicio: " + ejercicio.getNombre(), ex);
                    FirebaseCrashlytics.getInstance().recordException(ex); // Registro en Crashlytics
                }
            }
        }
        Toast.makeText(this, "Rutina guardada correctamente.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void agregarEjerciciosALaLista(String diaSeleccionado, String grupoMuscular, Ejercicio ejercicio) {
        // Asegurar que el mapa tiene una entrada para el día seleccionado
        ejerciciosPorDia.putIfAbsent(diaSeleccionado, new ArrayList<>());

        // Agregar el ejercicio a la lista correspondiente
        ejerciciosPorDia.get(diaSeleccionado).add(ejercicio);

        // Agregar el grupo muscular al encabezado del día si no está ya incluido
        if (!ejerciciosPorDía.containsKey(diaSeleccionado)) {
            ejerciciosPorDía.put(diaSeleccionado, new HashSet<>());
        }
        ejerciciosPorDía.get(diaSeleccionado).add(grupoMuscular);

        tieneCambiosSinGuardar = true; // Se ha modificado la lista de ejercicios
        actualizarListaEjercicios();
    }

    private void eliminarEjercicio(String dia, Ejercicio ejercicio) {
        List<Ejercicio> ejercicios = ejerciciosPorDia.get(dia);
        if (ejercicios != null) {
            ejercicios.remove(ejercicio);
            if (ejercicios.isEmpty()) {
                ejerciciosPorDia.remove(dia); // Si no hay más ejercicios, elimina el día
            }
            actualizarListaEjercicios(); // Actualizar la vista
            Toast.makeText(this, "Ejercicio eliminado", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoDiaSemana() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione un día de la semana");

        String[] diasSemana = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO"};
        builder.setItems(diasSemana, (dialog, which) -> {
            // Día seleccionado por el usuario
            String diaSeleccionado = diasSemana[which];
            Toast.makeText(this, "Día seleccionado: " + diaSeleccionado, Toast.LENGTH_SHORT).show();

            // Llama al metodo para seleccionar el grupo muscular
            obtenerGruposMusculares(diaSeleccionado);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void mostrarDialogoGruposMusculares(String dia, List<String> gruposMusculares) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccione un grupo muscular");

        String[] arrayGruposMusculares = gruposMusculares.toArray(new String[0]);
        builder.setItems(arrayGruposMusculares, (dialog, which) -> {
            String grupoSeleccionado = arrayGruposMusculares[which];

            // Añadir grupo muscular al día correspondiente
            gruposMuscularesPorDia.computeIfAbsent(dia, k -> new HashSet<>()).add(grupoSeleccionado);

            // Proceder con la selección del ejercicio
            obtenerEjerciciosPorGrupoMuscular(dia, grupoSeleccionado);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void mostrarDialogoEjercicios(String diaSeleccionado, String grupoMuscular, List<String> ejercicios) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ejercicios de " + grupoMuscular);

        String[] arrayejercicios = ejercicios.toArray(new String[0]);
        builder.setItems(arrayejercicios, (dialog, which) -> {
            String ejerciciosSeleccionados = arrayejercicios[which];
            mostrarDialogoDetallesEjercicio(diaSeleccionado, grupoMuscular, ejerciciosSeleccionados);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void mostrarDialogoDetallesEjercicio(String diaSeleccionado, String grupoMuscular, String nombreEjercicio) {
        // Crear un cuadro de diálogo personalizado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_detalles_ejercicios, null);
        builder.setView(dialogView);

        // Referencias a los campos del diálogo
        EditText edtNumSeries = dialogView.findViewById(R.id.edtNumSeries);
        EditText edtNumReps = dialogView.findViewById(R.id.edtNumReps);
        EditText edtPeso = dialogView.findViewById(R.id.edtPeso);
        EditText edtDescripcion = dialogView.findViewById(R.id.edtDescripcion);

        // Botones del cuadro de diálogo
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            // Obtener los valores ingresados
            String numSeries = edtNumSeries.getText().toString().trim();
            String numReps = edtNumReps.getText().toString().trim();
            String peso = edtPeso.getText().toString().trim();
            String descripcion = edtDescripcion.getText().toString().trim();

            // Crear un objeto del ejercicio
            Ejercicio ejercicio = new Ejercicio(diaSeleccionado, grupoMuscular, nombreEjercicio, numSeries, numReps, peso, descripcion);

            // Llamar a addExerciseToList para agregar el ejercicio a la estructura de datos
            agregarEjerciciosALaLista(diaSeleccionado, grupoMuscular, ejercicio);

            // Actualizar la interfaz para mostrar los ejercicios
            actualizarListaEjercicios();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void mostrarDialogoEditarEjercicio(String dia, Ejercicio ejercicio) {
        View vistaDialogo = getLayoutInflater().inflate(R.layout.dialog_editar_ejercicio, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(vistaDialogo);

        EditText edtNombre = vistaDialogo.findViewById(R.id.edtNombreEjercicio);
        EditText edtSeries = vistaDialogo.findViewById(R.id.edtNumSeries);
        EditText edtReps = vistaDialogo.findViewById(R.id.edtNumReps);
        EditText edtPeso = vistaDialogo.findViewById(R.id.edtPeso);
        EditText edtDescripcion = vistaDialogo.findViewById(R.id.edtDescripcion);

        // Rellenar los campos con los datos actuales del ejercicio
        edtNombre.setText(ejercicio.getNombre());
        edtSeries.setText(String.valueOf(ejercicio.getNum_series()));
        edtReps.setText(ejercicio.getNum_repes());
        edtPeso.setText(String.valueOf(ejercicio.getPeso()));
        edtDescripcion.setText(ejercicio.getDescripcion());

        // Hacer que el campo de nombre sea clickable
        edtNombre.setFocusable(false);
        edtNombre.setClickable(true);
        edtNombre.setOnClickListener(v -> {
            // Iniciar el flujo de selección de grupo muscular y ejercicio
            obtenerGruposMuscularesParaEdicion(dia, ejercicio, vistaDialogo);
        });

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            // Actualizar los datos del ejercicio
            ejercicio.setNum_series(String.valueOf(Integer.parseInt(edtSeries.getText().toString())));
            ejercicio.setNum_repes(edtReps.getText().toString());
            ejercicio.setPeso(String.valueOf((int) Double.parseDouble(edtPeso.getText().toString())));
            ejercicio.setDescripcion(edtDescripcion.getText().toString());

            actualizarListaEjercicios(); // Actualizar la vista
            Toast.makeText(this, "Ejercicio actualizado", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void mostrarDialogoEjerciciosEdicion(String dia, Ejercicio ejercicio, String grupoMuscular, List<String> ejercicios, View vistaDialogo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona un ejercicio");

        String[] arrayEjercicios = ejercicios.toArray(new String[0]);
        builder.setItems(arrayEjercicios, (dialog, which) -> {
            String ejercicioSeleccionado = arrayEjercicios[which];

            // Actualizar los datos del ejercicio
            ejercicio.setGrupo_muscular(grupoMuscular);
            ejercicio.setNombre(ejercicioSeleccionado);

            // Actualizar el nombre en el cuadro de diálogo
            EditText edtNombre = vistaDialogo.findViewById(R.id.edtNombreEjercicio);
            if (edtNombre != null) {
                edtNombre.setText(ejercicioSeleccionado);
            }

            actualizarListaEjercicios(); // Actualizar la vista general
            Toast.makeText(this, "Ejercicio actualizado", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoOpcionesEjercicio(String dia, Ejercicio ejercicio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones para el ejercicio")
                .setItems(new String[]{"Editar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoEditarEjercicio(dia, ejercicio);
                    } else if (which == 1) {
                        eliminarEjercicio(dia, ejercicio);
                    }
                });
        builder.create().show();
    }

    private void mostrarDialogoGruposMuscularesEdicion(String dia, Ejercicio ejercicio, List<String> gruposMusculares, View vistaDialogo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona un grupo muscular");

        String[] arrayGruposMusculares = gruposMusculares.toArray(new String[0]);
        builder.setItems(arrayGruposMusculares, (dialog, which) -> {
            String grupoMuscularSeleccionado = arrayGruposMusculares[which];

            // Proceder con la selección del ejercicio
            obtenerEjerciciosPorGrupoParaEdicion(dia, ejercicio, grupoMuscularSeleccionado, vistaDialogo);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoCambiosSinGuardar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambios sin guardar");
        builder.setMessage("¿Deseas guardar los cambios antes de salir?");
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            guardarRutinaEnFirestore(); // Guardar cambios
        });
        builder.setNegativeButton("Salir sin guardar", (dialog, which) -> {
            finish(); // Salir directamente
        });
        builder.setNeutralButton("Cancelar", (dialog, which) -> {
            dialog.dismiss(); // Cerrar el diálogo y permanecer en la pantalla
        });
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Vuelve a la actividad previa )
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (tieneCambiosSinGuardar) {
            mostrarDialogoCambiosSinGuardar();
        } else {
            super.onBackPressed();
        }
    }

}