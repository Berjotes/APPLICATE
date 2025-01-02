package com.example.applicate.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicate.R;
import com.example.applicate.modelos.Rutina;

import java.util.List;

public class AdaptadorRutina extends RecyclerView.Adapter<AdaptadorRutina.ViewHolder> {

    private List<Rutina> rutinas;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(Rutina rutina);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Rutina rutina);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public AdaptadorRutina(List<Rutina> rutinas) {
        this.rutinas = rutinas != null ? rutinas : List.of(); // Asegura que nunca sea null
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina, parent, false); // Layout personalizado
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rutina rutina = rutinas.get(position);
        holder.textoNumeroRutina.setText("Rutina #" + rutina.getNumRutina());
        holder.textoFechaRutina.setText("Fecha: " + rutina.getFechaCreacion());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(rutina);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(rutina);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return rutinas.size();
    }

    /**
     * ViewHolder para representar cada rutina.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textoNumeroRutina;
        TextView textoFechaRutina;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textoNumeroRutina = itemView.findViewById(R.id.textNumeroRutina);
            textoFechaRutina = itemView.findViewById(R.id.textFechaRutina);
        }
    }

    //Metodo para actualizar la lista de rutinas
    public void actualizarDatos(List<Rutina> nuevasRutinas) {
        this.rutinas = nuevasRutinas != null ? nuevasRutinas : List.of();
        notifyDataSetChanged(); // Refresca el RecyclerView
    }
}
