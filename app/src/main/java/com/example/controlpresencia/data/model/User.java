package com.example.controlpresencia.data.model;

public class User {
    private int id_trabajador;
    private String nombre;
    private String email;
    private Empresa empresa;

    public Empresa getEmpresa() { return empresa; }
    public String getNombre() { return nombre; }
}