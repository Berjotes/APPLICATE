package com.example.applicate;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddClientActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone, edtBirthDate;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_client);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtBirthDate = findViewById(R.id.edtBirthDate);
        btnSave = findViewById(R.id.btnSave);

        // Configurar el DatePicker para el EditText de fecha de nacimiento
        edtBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Configurar el botón de guardar
        btnSave.setOnClickListener(v -> {
            saveClientData();
        });
    }

    // Método para mostrar el DatePickerDialog
    private void showDatePickerDialog() {
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear y mostrar el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddClientActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    // Mostrar la fecha seleccionada en el EditText
                    String birthDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    edtBirthDate.setText(birthDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private void saveClientData() {
        String nombre = edtName.getText().toString();
        String email = edtEmail.getText().toString();
        String telefono = edtPhone.getText().toString();
        String fecha_nacimiento = edtBirthDate.getText().toString();

        // Crear un cliente en un mapa
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
                    Toast.makeText(AddClientActivity.this, "Cliente guardado", Toast.LENGTH_SHORT).show();
                    finish();  // Cerrar la actividad después de guardar
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddClientActivity.this, "Error al guardar cliente", Toast.LENGTH_SHORT).show();
                });
    }

}
