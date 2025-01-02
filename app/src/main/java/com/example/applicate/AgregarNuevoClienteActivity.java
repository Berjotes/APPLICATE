package com.example.applicate;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AgregarNuevoClienteActivity extends AppCompatActivity {

    // Variables globales (Campos y Botón)
    private EditText edtNombre, edtEmail, edtTelefono, edtFechaNacimiento;
    private Button btnGuardar;

    // Instancia de la base de datos Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogo_agregar_cliente);

        edtNombre = findViewById(R.id.edtNombre);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtFechaNacimiento = findViewById(R.id.edtFechaNacimiento);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Configurar el DatePicker para el EditText de fecha de nacimiento
        edtFechaNacimiento.setOnClickListener(v -> mostrarSelectorFecha());

        // Configurar el botón de guardar
        btnGuardar.setOnClickListener(v -> {
            guardarDatosCliente();
        });
    }

    // Mostrar un DatePickerDialog para seleccionar la fecha
    private void mostrarSelectorFecha() {
        // Obtener la fecha actual
        Calendar calendario = Calendar.getInstance();
        int year = calendario.get(Calendar.YEAR);
        int month = calendario.get(Calendar.MONTH);
        int day = calendario.get(Calendar.DAY_OF_MONTH);

        // Crear y mostrar el DatePickerDialog
        DatePickerDialog seleccionarFecha = new DatePickerDialog(
                AgregarNuevoClienteActivity.this,
                (view, anyo, mes, diaDelMes) -> {
                    // Mostrar la fecha seleccionada en el EditText
                    String fechaNacimiento = diaDelMes + "/" + (mes + 1) + "/" + anyo;
                    edtFechaNacimiento.setText(fechaNacimiento);
                },
                year, month, day);
        seleccionarFecha.show();
    }

    // Guardar los datos del cliente en Firestore
    private void guardarDatosCliente() {
        String nombre = edtNombre.getText().toString();
        String email = edtEmail.getText().toString();
        String telefono = edtTelefono.getText().toString();
        String fecha_nacimiento = edtFechaNacimiento.getText().toString();

        Map<String, Object> client = new HashMap<>();
        client.put("nombre", nombre);
        client.put("email", email);
        client.put("telefono", telefono);
        client.put("fecha_nacimiento", fecha_nacimiento);

        // Generar un id para el cliente automáticamente
        String clientId = "C" + String.format("%04d", new Random().nextInt(10000));

        // Guardar el cliente en Firestore
        db.collection("CLIENTES").document(clientId)
                .set(client)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AgregarNuevoClienteActivity.this, "Cliente guardado", Toast.LENGTH_SHORT).show();
                    finish();  // Cerrar la actividad después de guardar
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgregarNuevoClienteActivity.this, "Error al guardar cliente", Toast.LENGTH_SHORT).show();
                });
    }

}