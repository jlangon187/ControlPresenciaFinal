package com.example.controlpresencia.data.model;

public class ConfigUbicacionRequest {
    private double latitud;
    private double longitud;
    private double radio;
    private Integer empresa_id;

    public ConfigUbicacionRequest(double latitud, double longitud, double radio, Integer empresa_id) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.radio = radio;
        this.empresa_id = empresa_id;
        }
}