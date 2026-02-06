package com.example.controlpresencia.data.model;

public class FichajeRequest {
    private Double latitud;
    private Double longitud;

    public FichajeRequest(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Getters y Setters
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}