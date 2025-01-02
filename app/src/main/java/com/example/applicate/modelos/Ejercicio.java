package com.example.applicate.modelos;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Ejercicio {
    private String dia_semana;
    private String grupo_muscular;
    private String nombre;
    private String num_series;
    private String num_repes;
    private String peso;
    private String descripcion;
    private int posicion;

    public Ejercicio(String dia_semana, String grupo_muscular, String nombre, String num_series, String num_repes, String peso, String descripcion) {
        this.dia_semana = dia_semana;
        this.grupo_muscular = grupo_muscular;
        this.nombre = nombre;
        this.num_series = num_series;
        this.num_repes = num_repes;
        this.peso = peso;
        this.descripcion = descripcion;
    }

    public void setDia_semana(String dia_semana) {
        this.dia_semana = dia_semana;
    }
    public void setGrupo_muscular(String grupo_muscular) {
        this.grupo_muscular = grupo_muscular;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setNum_series(String num_series) {
        this.num_series = num_series;
    }
    public void setNum_repes(String num_repes) {
        this.num_repes = num_repes;
    }
    public void setPeso(String peso) {
        this.peso = peso;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public String getDia_semana() {
        return dia_semana;
    }
    public String getGrupoMuscular() {
        return grupo_muscular;
    }
    public String getNombre() {
        return nombre;
    }
    public String getNum_series() {
        return num_series;
    }
    public String getNum_repes() {
        return num_repes;
    }
    public String getPeso() {
        return peso;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public int getPosicion() {
        return posicion;
    }

    public Map<String, Object> toMap(String diaSemana) {
        Map<String, Object> map = new HashMap<>();
        map.put("dia_semana", diaSemana);
        map.put("grupo_muscular", this.grupo_muscular);
        map.put("nombre_ejercicio", this.nombre);
        map.put("num_series", this.num_series);
        map.put("num_repeticiones", this.num_repes);
        map.put("peso", this.peso);
        map.put("descripcion", this.descripcion);
        return map;
    }

    public static Ejercicio fromDocument(QueryDocumentSnapshot document) {
        String diaSemana = document.getString("dia_semana");
        String grupoMuscular = document.getString("grupo_muscular");
        String nombreEjercicio = document.getString("nombre_ejercicio");
        String numSeries = document.getString("num_series");
        String numRepeticiones = document.getString("num_repeticiones");
        String peso = document.getString("peso");
        String descripcion = document.getString("descripcion");

        return new Ejercicio(diaSemana, grupoMuscular, nombreEjercicio, numSeries, numRepeticiones, peso, descripcion);
    }


}
