package com.example.applicate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicate.adaptadores.AdaptadorCliente;
import com.example.applicate.modelos.Cliente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorClientesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdaptadorCliente adaptadorCliente;
    private Button btnAgregarCliente; // Botón para abrir el diálogo
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_clientes);

        // Configurar el botón para forzar un fallo
        //Button btnCrash = findViewById(R.id.btnCrash);
        //btnCrash.setOnClickListener(v -> {
        //    Log.d("CrashlyticsTest", "Se ha pulsado el botón de crash en la lista de clientes");
        //    throw new RuntimeException("Prueba de Crashlytics - Forzando un fallo en la lista de clientes");
        //});

        // Configurar Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Usa el Toolbar como ActionBar
        getSupportActionBar().setTitle("CLIENTES");

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerView);

        // Botón para añadir clientes
        btnAgregarCliente = findViewById(R.id.btnAgregarCliente);
        btnAgregarCliente.setOnClickListener(v -> mostrarDialogoAgregarCliente());

        // Llamada para obtener los datos de Firebase
        obtenerClientes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Gestionar la seleccion de ítems del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.cerrar_sesion) {
            cerrarSesion(); // Llamar al metodo de cerrar sesión
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Metodo para cerrar sesion
    private void cerrarSesion() {
        // Cerrar sesion de Firebase
        FirebaseAuth.getInstance().signOut();
        // Mostrar mensaje de "Sesion cerrada"
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(GestorClientesActivity.this, LoginActivity.class);
        startActivity(intent);
        // Finalizar la actividad actual para que no se pueda regresar
        finish();
    }

    private void obtenerClientes() {
        db.collection("CLIENTES")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> nombresClientes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombreCliente = document.getString("nombre");
                            if (nombreCliente != null) {
                                nombresClientes.add(nombreCliente);
                            }
                        }
                        // Ordenar los nombres alfabéticamente
                        nombresClientes.sort(String::compareToIgnoreCase);
                        // Actualizar el RecyclerView
                        actualizarVistaClientes(nombresClientes);
                    } else {
                        Log.w("ListaClientesActivity", "Error al obtener documentos.", task.getException());
                    }
                });
    }

    private void actualizarVistaClientes(List<String> nombresClientes) {
        if (adaptadorCliente == null) {
            adaptadorCliente = new AdaptadorCliente(nombresClientes);
            adaptadorCliente.setOnItemClickListener(nombreCliente -> {
                db.collection("CLIENTES")
                        .whereEqualTo("nombre", nombreCliente)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String idCliente = document.getId(); // Obtener el ID del documento del cliente
                                Intent intent = new Intent(GestorClientesActivity.this, RutinasClientesActivity.class);
                                intent.putExtra("idCliente", idCliente);
                                intent.putExtra("nombreCliente", nombreCliente); // Asegúrate de pasar el nombre del cliente
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(GestorClientesActivity.this, "Error al cargar las rutinas del cliente", Toast.LENGTH_SHORT).show();
                        });
            });

            adaptadorCliente.setOnItemLongClickListener(clientName -> {
                mostrarDialogoEditarEliminar(clientName);
                return true; // El evento ha sido manejado
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adaptadorCliente);
        } else {
            adaptadorCliente.actualizarDatos(nombresClientes);
        }
    }

    private void agregarClienteAFirebase(String nombre, String email, String telefono, String fechaNacimiento) {
        db.collection("CLIENTES")
                .whereEqualTo("nombre", nombre)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        crearCliente(nombre, email, telefono, fechaNacimiento);
                    } else {
                        Toast.makeText(this, "Ya existe un cliente con ese nombre.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void crearCliente(String nombre, String email, String telefono, String fechaNacimiento) {
        Map<String, Object> cliente = new HashMap<>();
        cliente.put("nombre", nombre);
        cliente.put("email", email);
        cliente.put("telefono", telefono);
        cliente.put("fecha_nacimiento", fechaNacimiento);

        db.collection("CLIENTES")
                .add(cliente)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Cliente añadido exitosamente.", Toast.LENGTH_SHORT).show();
                    obtenerClientes();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al añadir cliente.", Toast.LENGTH_SHORT).show();
                });
    }


    private void mostrarDialogoAgregarCliente() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_cliente, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        EditText edtNombre = dialogView.findViewById(R.id.edtNombre);
        EditText edtEmail = dialogView.findViewById(R.id.edtEmail);
        EditText edtTelefono = dialogView.findViewById(R.id.edtTelefono);
        EditText edtFechaNacimiento = dialogView.findViewById(R.id.edtFechaNacimiento);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        edtFechaNacimiento.setOnClickListener(v -> mostrarSelectorFecha(edtFechaNacimiento, null));

        AlertDialog dialog = builder.create();

        btnGuardar.setOnClickListener(v -> {
            String nombre = edtNombre.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String telefono = edtTelefono.getText().toString().trim();
            String fechaNacimiento = edtFechaNacimiento.getText().toString().trim();

            // Validar los datos antes de guardarlos
            if (!validarDatos(nombre, email, telefono, fechaNacimiento)) {
                return;
            }

            agregarClienteAFirebase(nombre, email, telefono, fechaNacimiento);
            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean validarDatos(String nombre, String email, String telefono, String fechaNacimiento) {
        // Validar que el nombre no contenga números
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            Toast.makeText(this, "El nombre solo puede contener letras.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que el teléfono no esté vacío
        if (telefono.isEmpty()) {
            Toast.makeText(this, "El teléfono es obligatorio.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que el cliente tenga al menos 14 años
        if (!validarEdadMinima(fechaNacimiento, 14)) {
            Toast.makeText(this, "El cliente debe tener al menos 14 años.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validarEdadMinima(String fechaNacimiento, int edadMinima) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date fechaNac = sdf.parse(fechaNacimiento);

            Calendar hoy = Calendar.getInstance();
            Calendar fechaNacCal = Calendar.getInstance();
            fechaNacCal.setTime(fechaNac);

            int edad = hoy.get(Calendar.YEAR) - fechaNacCal.get(Calendar.YEAR);

            if (hoy.get(Calendar.MONTH) < fechaNacCal.get(Calendar.MONTH) ||
                    (hoy.get(Calendar.MONTH) == fechaNacCal.get(Calendar.MONTH) &&
                            hoy.get(Calendar.DAY_OF_MONTH) < fechaNacCal.get(Calendar.DAY_OF_MONTH))) {
                edad--;
            }

            return edad >= edadMinima;
        } catch (ParseException e) {
            Toast.makeText(this, "Error en el formato de la fecha.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    private void mostrarDialogoEditarEliminar(String nombreCliente) {
        CharSequence options[] = new CharSequence[]{"Editar", "Eliminar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de Cliente");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Editar
                    editarCliente(nombreCliente);
                    break;
                case 1: // Eliminar
                    eliminarCliente(nombreCliente);
                    break;
            }
        });
        builder.show();
    }

    private void editarCliente(String nombreCliente) {
        db.collection("CLIENTES")
                .whereEqualTo("nombre", nombreCliente)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Cliente cliente = document.toObject(Cliente.class);
                        AlertDialog.Builder builder = new AlertDialog.Builder(GestorClientesActivity.this);
                        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_cliente, null);
                        builder.setView(dialogView);

                        EditText edtNombre = dialogView.findViewById(R.id.edtNombre);
                        EditText edtEmail = dialogView.findViewById(R.id.edtEmail);
                        EditText edtTelefono = dialogView.findViewById(R.id.edtTelefono);
                        EditText edtFechaNacimiento = dialogView.findViewById(R.id.edtFechaNacimiento);
                        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

                        edtNombre.setText(cliente.getNombre());
                        edtEmail.setText(cliente.getEmail());
                        edtTelefono.setText(cliente.getTelefono());
                        edtFechaNacimiento.setText(cliente.getFecha_nacimiento());

                        edtFechaNacimiento.setOnClickListener(v -> mostrarSelectorFecha(edtFechaNacimiento, cliente.getFecha_nacimiento()));

                        btnGuardar.setText("Actualizar");
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        btnGuardar.setOnClickListener(v -> {
                            String nombre = edtNombre.getText().toString().trim();
                            String email = edtEmail.getText().toString().trim();
                            String telefono = edtTelefono.getText().toString().trim();
                            String fechaNacimiento = edtFechaNacimiento.getText().toString().trim();

                            // Validar los datos antes de actualizar
                            if (!validarDatos(nombre, email, telefono, fechaNacimiento)) {
                                return;
                            }

                            actualizarDatosCliente(document.getId(), nombre, email, telefono, fechaNacimiento);
                            dialog.dismiss();
                        });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(GestorClientesActivity.this, "Error al cargar datos del cliente", Toast.LENGTH_SHORT).show());
    }

    private void eliminarCliente(String nombreCliente) {
        // Crear un AlertDialog para confirmar la eliminación
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cliente")
                .setMessage("¿Estás seguro de eliminar a " + nombreCliente + "? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Usuario confirma la eliminación, se elimina el cliente
                    db.collection("CLIENTES")
                            .whereEqualTo("nombre", nombreCliente)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        db.collection("CLIENTES").document(document.getId()).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(GestorClientesActivity.this, "Cliente eliminado correctamente.", Toast.LENGTH_SHORT).show();
                                                    obtenerClientes(); // Actualizar la lista después de eliminar
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(GestorClientesActivity.this, "Error al eliminar cliente.", Toast.LENGTH_SHORT).show();
                                                    Log.e("ListaClientesActivity", "Error al eliminar cliente", e);
                                                });
                                    }
                                } else {
                                    Log.w("ListaClientesActivity", "Error buscando cliente para eliminar", task.getException());
                                    Toast.makeText(GestorClientesActivity.this, "Error al encontrar cliente para eliminar.", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null) // Si el usuario selecciona "Cancelar", no hacer nada
                .show();
    }

    private void actualizarDatosCliente(String docId, String nombre, String email, String telefono, String fechaNacimiento) {
        Map<String, Object> cliente = new HashMap<>();
        cliente.put("nombre", nombre);
        cliente.put("email", email);
        cliente.put("telefono", telefono);
        cliente.put("fecha_nacimiento", fechaNacimiento);

        db.collection("CLIENTES").document(docId)
                .set(cliente)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GestorClientesActivity.this, "Datos del cliente actualizados", Toast.LENGTH_SHORT).show();
                    obtenerClientes();  // Actualizar la lista
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GestorClientesActivity.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                });
    }

    //Muestra un DatePickerDialog para seleccionar una fecha.
    private void mostrarSelectorFecha(EditText edtFechaNacimiento, String fechaActual) {
        // Obtener la fecha actual o usar la fecha proporcionada
        Calendar calendario = Calendar.getInstance();
        int anyo, mes, dia;

        if (fechaActual != null && !fechaActual.isEmpty()) {
            String[] partesFecha = fechaActual.split("/");
            dia = Integer.parseInt(partesFecha[0]);
            mes = Integer.parseInt(partesFecha[1]) - 1;
            anyo = Integer.parseInt(partesFecha[2]);
        } else {
            anyo = calendario.get(Calendar.YEAR);
            mes = calendario.get(Calendar.MONTH);
            dia = calendario.get(Calendar.DAY_OF_MONTH);
        }

        // Mostrar el DatePickerDialog
        DatePickerDialog dialogoFecha = new DatePickerDialog(
                this,
                (view, anyoSelec, mesSelec, diaSelec) -> {
                    // Formatear y asignar la fecha seleccionada al EditText
                    String fechaFormateada = diaSelec + "/" + (mesSelec + 1) + "/" + anyoSelec;
                    edtFechaNacimiento.setText(fechaFormateada);
                },
                anyo, mes, dia
        );
        dialogoFecha.show();
    }

}