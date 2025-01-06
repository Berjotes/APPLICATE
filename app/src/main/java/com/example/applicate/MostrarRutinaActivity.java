package com.example.applicate;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MostrarRutinaActivity extends AppCompatActivity {

    private TextView tvMostrarRutina;
    private Button btnCompartirRutina;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_rutina);

        // Configuración del Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Mostrar botón de atrás
            getSupportActionBar().setTitle("Detalles de la Rutina"); // Establecer título
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Acción del botón atrás

        tvMostrarRutina = findViewById(R.id.tvMostrarRutina);
        btnCompartirRutina = findViewById(R.id.btnCompartirRutina);

        db = FirebaseFirestore.getInstance();

        String idCliente = getIntent().getStringExtra("idCliente");
        String idRutina = getIntent().getStringExtra("idRutina");

        Log.d("MostrarRutinaActivity", "ID Cliente: " + idCliente + ", ID Rutina: " + idRutina);

        if (idCliente != null && idRutina != null) {
            cargarRutina(idCliente, idRutina);
        } else {
            Toast.makeText(this, "Error al cargar los datos de la rutina.", Toast.LENGTH_SHORT).show();
        }

        btnCompartirRutina.setOnClickListener(v -> {
            String datos = tvMostrarRutina.getText().toString();
            if (datos.isEmpty()) {
                Toast.makeText(this, "No hay contenido para compartir.", Toast.LENGTH_SHORT).show();
            } else {
                crearYCompartirPDF(datos);
            }
        });
    }


    //Carga los ejercicios de la rutina desde Firestore y los agrupa por día.
    private void cargarRutina(String idCliente, String idRutina) {
        db.collection("CLIENTES").document(idCliente)
                .collection("RUTINAS").document(idRutina)
                .collection("EJERCICIOS_RUTINA")
                .orderBy("posicion") // Ordenar por posición
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvMostrarRutina.setText("No hay ejercicios en esta rutina.");
                        return;
                    }

                    // Agrupar los ejercicios por día
                    Map<String, List<Map<String, String>>> ejerciciosAgrupadosPorDia = new HashMap<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Obtener datos
                        String diaSemana = document.getString("dia_semana");
                        String nombreEjercicio = document.getString("nombre_ejercicio");
                        String numSeries = document.getString("num_series");
                        String numRepeticiones = document.getString("num_repeticiones");
                        String descripcion = document.getString("descripcion");
                        String peso = document.getString("peso");
                        String grupoMuscular = document.getString("grupo_muscular"); // Asegúrate de que existe en Firestore

                        if (diaSemana == null) diaSemana = "SIN DÍA";

                        // Crear una estructura de datos para el ejercicio
                        Map<String, String> datosEjercicio = new HashMap<>();
                        datosEjercicio.put("nombre", nombreEjercicio);
                        datosEjercicio.put("series", numSeries);
                        datosEjercicio.put("repeticiones", numRepeticiones);
                        datosEjercicio.put("peso", peso);
                        datosEjercicio.put("descripcion", descripcion);
                        datosEjercicio.put("grupo_muscular", grupoMuscular); // Añadir grupo muscular

                        ejerciciosAgrupadosPorDia.putIfAbsent(diaSemana, new ArrayList<>());
                        ejerciciosAgrupadosPorDia.get(diaSemana).add(datosEjercicio);
                    }

                    // Llamar al metodo para mostrar los ejercicios ordenados
                    mostrarRutina(ejerciciosAgrupadosPorDia);
                })
                .addOnFailureListener(e -> {
                    FirebaseCrashlytics.getInstance().recordException(e); // Registro del error
                    Toast.makeText(this, "Error al cargar los ejercicios de la rutina.", Toast.LENGTH_SHORT).show();
                    Log.e("MostrarRutinaActivity", "Error al obtener ejercicios", e);
                });
    }


    //Muestra los ejercicios agrupados por día en el TextView.
    private void mostrarRutina(Map<String, List<Map<String, String>>> ejerciciosAgrupadosPorDia) {
        List<String> diasOrdenados = Arrays.asList("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO");

        SpannableStringBuilder textoRutina = new SpannableStringBuilder();

        for (String dia : diasOrdenados) {
            if (!ejerciciosAgrupadosPorDia.containsKey(dia)) {
                continue;
            }

            List<Map<String, String>> ejercicios = ejerciciosAgrupadosPorDia.get(dia);

            Set<String> gruposMusculares = new HashSet<>();
            for (Map<String, String> ejercicio : ejercicios) {
                String grupoMuscular = ejercicio.get("grupo_muscular");
                if (grupoMuscular != null && !grupoMuscular.isEmpty()) {
                    gruposMusculares.add(grupoMuscular);
                }
            }

            textoRutina.append(dia.toUpperCase())
                    .append(" (")
                    .append(String.join(", ", gruposMusculares))
                    .append(")\n");

            for (Map<String, String> ejercicio : ejercicios) {
                String nombre = ejercicio.get("nombre");
                String series = ejercicio.get("series");
                String repeticiones = ejercicio.get("repeticiones");
                String peso = ejercicio.get("peso");
                String descripcion = ejercicio.get("descripcion");

                int start = textoRutina.length();
                textoRutina.append("- ").append(nombre != null ? nombre : "Sin nombre");
                textoRutina.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        start, textoRutina.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);

                textoRutina.append("\n")
                        .append("  Series: ").append(series != null ? series : "N/A")
                        .append("  |  Repeticiones: ").append(repeticiones != null ? repeticiones : "N/A")
                        .append("  |  Peso: ").append(peso != null ? peso : "N/A").append(" kg\n")
                        .append("  Descripción: ").append(descripcion != null ? descripcion : "Sin descripción").append("\n\n");
            }
        }

        if (textoRutina.length() == 0) {
            tvMostrarRutina.setText("No hay ejercicios disponibles para esta rutina.");
        } else {
            tvMostrarRutina.setText(textoRutina);
        }
    }

    private void crearYCompartirPDF(String contenido) {
        String idCliente = getIntent().getStringExtra("idCliente");
        String idRutina = getIntent().getStringExtra("idRutina");

        if (idCliente == null || idRutina == null) {
            Toast.makeText(this, "Error: Cliente o Rutina no válidos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el nombre del cliente y el número de rutina desde Firestore
        db.collection("CLIENTES").document(idCliente)
                .get()
                .addOnSuccessListener(clientDocument -> {
                    String nombreCliente = clientDocument.getString("nombre");

                    db.collection("CLIENTES").document(idCliente)
                            .collection("RUTINAS").document(idRutina)
                            .get()
                            .addOnSuccessListener(routineDocument -> {
                                Long numRutina = routineDocument.getLong("num_rutina");
                                String fechaRutina = routineDocument.getString("fecha_creacion");

                                if (nombreCliente == null || numRutina == null || fechaRutina == null) {
                                    Toast.makeText(this, "Error al obtener los datos para el PDF.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Generar el nombre del PDF
                                String nombrePDF = "Rutina_" + nombreCliente + "_" + numRutina + ".pdf";

                                // Crear el PDF
                                crearPDF(nombrePDF, contenido, nombreCliente, fechaRutina);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al obtener la rutina.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Toast.makeText(this, "Error al obtener el cliente.", Toast.LENGTH_SHORT).show();
                });
    }

    private void crearPDF(String nombrePDF, String contenido, String cliente, String fechaRutina) {
        try {
            File archivo = new File(getExternalFilesDir(null), nombrePDF);
            PdfDocument pdfDocument = new PdfDocument();
            Paint paint = new Paint();
            Paint paintBold = new Paint(paint); // Copiar el estilo base
            paintBold.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Aplicar negrita

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            paint.setTextSize(12);
            paintBold.setTextSize(12);

            int x = 10, y = 25;

            // Añadir cabecera con cliente y fecha
            canvas.drawText("Cliente: " + cliente, x, y, paint);
            y += 20;
            canvas.drawText("Fecha de creación: " + fechaRutina, x, y, paint);
            y += 30;

            for (String line : contenido.split("\n")) {
                if (line.startsWith("- ")) {
                    // Negrita para los nombres de los ejercicios
                    int separatorIndex = line.indexOf(" → ");
                    if (separatorIndex > 0) {
                        canvas.drawText(line.substring(0, separatorIndex), x, y, paintBold);
                        canvas.drawText(line.substring(separatorIndex), x + paintBold.measureText(line.substring(0, separatorIndex)), y, paint);
                    } else {
                        canvas.drawText(line, x, y, paintBold);
                    }
                } else {
                    canvas.drawText(line, x, y, paint);
                }

                y += 15;
                if (y > pageInfo.getPageHeight() - 50) {
                    pdfDocument.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 25;
                }
            }
            pdfDocument.finishPage(page);

            FileOutputStream fos = new FileOutputStream(archivo);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            compartirPDF(archivo);

        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e); // Registro del error
            Toast.makeText(this, "Error al generar el PDF.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void compartirPDF(File archivo) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    "com.example.applicate.provider",
                    archivo
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Compartir rutina"));
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e); // Registro del error
            Toast.makeText(this, "Error al compartir el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


}
