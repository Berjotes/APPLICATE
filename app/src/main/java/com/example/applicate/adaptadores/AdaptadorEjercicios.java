package com.example.applicate.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicate.R;
import com.example.applicate.modelos.Ejercicio;

import java.util.List;

/**
 * Adaptador para mostrar una lista de ejercicios en un RecyclerView.
 */
public class AdaptadorEjercicios extends RecyclerView.Adapter<AdaptadorEjercicios.ViewHolder> {

    private final List<Ejercicio> listaEjercicios;

    public AdaptadorEjercicios(List<Ejercicio> listaEjercicios) {
        this.listaEjercicios = listaEjercicios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ejercicio ejercicio = listaEjercicios.get(position);
        holder.textoNombreEjercicio.setText(ejercicio.getNombre());
        holder.textoGrupoMuscular.setText("Grupo: " + ejercicio.getGrupoMuscular());
        holder.textoDiaSemana.setText("Día: " + ejercicio.getDia_semana());
    }

    @Override
    public int getItemCount() {
        return listaEjercicios != null ? listaEjercicios.size() : 0;
    }

    /**
     * ViewHolder para representar un ejercicio en el RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textoNombreEjercicio, textoGrupoMuscular, textoDiaSemana;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textoNombreEjercicio = itemView.findViewById(R.id.textNombreEjercicio);
            textoGrupoMuscular = itemView.findViewById(R.id.textGrupoMuscular);
            textoDiaSemana = itemView.findViewById(R.id.textDiaSemana);
        }
    }
}
