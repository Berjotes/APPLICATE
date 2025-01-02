package com.example.applicate.modelos;

public class Cliente {
    private String nombre;
    private String email;
    private String telefono;
    private String fecha_nacimiento;

    // Constructor
    public Cliente() {
    }

    public Cliente(String nombre, String email, String telefono, String fecha_nacimiento) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fecha_nacimiento = fecha_nacimiento;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    // Setters
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setFecha_nacimiento(String fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

}