package com.example.controlpresencia.data.model;

public class Empresa {
    private String nombrecomercial;
    private Double latitud;
    private Double longitud;
    private Integer radio;

    // Getters
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public Integer getRadio() { return radio; }
    public String getNombre() { return nombrecomercial; }
}