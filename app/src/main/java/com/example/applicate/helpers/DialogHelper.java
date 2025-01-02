package com.example.applicate.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

import java.util.List;

/**
 * Clase de ayuda para manejar diálogos de manera centralizada.
 */
public class DialogHelper {

    public static void mostrarDialogoSimple(Context context, String titulo, List<String> items, DialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titulo);
        builder.setItems(items.toArray(new String[0]), (dialog, which) -> callback.onItemSelected(items.get(which)));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public static void mostrarDialogoDetallesEjercicio(Context context, String ejercicio, DetallesCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Detalles de " + ejercicio);

        EditText inputDetalles = new EditText(context);
        builder.setView(inputDetalles);

        builder.setPositiveButton("Guardar", (dialog, which) -> callback.onDetallesIngresados(inputDetalles.getText().toString()));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    public interface DialogCallback {
        void onItemSelected(String item);
    }

    public interface DetallesCallback {
        void onDetallesIngresados(String detalles);
    }
}
