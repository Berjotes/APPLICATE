package com.example.applicate.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdaptadorRutinasClientes extends RecyclerView.Adapter<AdaptadorRutinasClientes.ViewHolder> {

    private List<String> rutinas;

    public AdaptadorRutinasClientes(List<String> rutinas) {
        this.rutinas = rutinas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int posicion) {
        holder.textView.setText(rutinas.get(posicion));
    }

    @Override
    public int getItemCount() {
        return rutinas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

}