package com.example.applicate.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicate.R;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.List;

/**
 * Adaptador para mostrar una lista de clientes en un RecyclerView.
 */
public class AdaptadorCliente extends RecyclerView.Adapter<AdaptadorCliente.VistaCliente> {

    private List<String> nombresClientes;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(String nombreCliente);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(String nombreCliente);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public AdaptadorCliente(List<String> nombresClientes) {
        this.nombresClientes = nombresClientes;
    }

    @NonNull
    @Override
    public VistaCliente onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new VistaCliente(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull VistaCliente holder, int position) {
        String nombreCliente = nombresClientes.get(position);
        holder.textoNombreCliente.setText(nombreCliente);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                // Registrar clic en Crashlytics
                FirebaseCrashlytics.getInstance().log("Cliente seleccionado: " + nombreCliente);
                onItemClickListener.onItemClick(nombreCliente);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                // Registrar clic largo en Crashlytics
                FirebaseCrashlytics.getInstance().log("Clic largo en cliente: " + nombreCliente);
                return onItemLongClickListener.onItemLongClick(nombreCliente);
            }
            return false;
        });
    }


    @Override
    public int getItemCount() {
        return nombresClientes != null ? nombresClientes.size() : 0;
    }

    public static class VistaCliente extends RecyclerView.ViewHolder {
        TextView textoNombreCliente;

        public VistaCliente(@NonNull View itemView) {
            super(itemView);
            textoNombreCliente = itemView.findViewById(R.id.textNombreCliente);
        }
    }

    /**
     * Método para actualizar la lista de clientes.
     */
    public void actualizarDatos(List<String> nuevosNombres) {
        this.nombresClientes = nuevosNombres;
        notifyDataSetChanged(); // Refresca toda la lista
    }
}
