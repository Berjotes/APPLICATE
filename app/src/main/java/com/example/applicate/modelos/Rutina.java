package com.example.applicate.modelos;

public class Rutina {
    private int numRutina;
    private String fechaCreacion;

    public Rutina(int numRutina, String fechaCreacion) {
        this.numRutina = numRutina;
        this.fechaCreacion = fechaCreacion;
    }

    public int getNumRutina() {
        return numRutina;
    }

    public void setNumRutina(int numRutina) {
        this.numRutina = numRutina;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
