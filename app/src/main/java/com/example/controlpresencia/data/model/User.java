package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id_trabajador")
    private int idTrabajador;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellidos")
    private String apellidos;

    @SerializedName("email")
    private String email;

    @SerializedName("empresa")
    private Empresa empresa;

    // Getters
    public int getIdTrabajador() { return idTrabajador; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public Empresa getEmpresa() { return empresa; }
    public String getNombreCompleto() {
        return nombre + " " + (apellidos != null ? apellidos : "");
    }
}