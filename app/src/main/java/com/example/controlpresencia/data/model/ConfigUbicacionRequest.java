package com.example.controlpresencia.data.model;

// Se usa para mandarle al servidor la ubicación de la empresa y el radio permitido.
public class ConfigUbicacionRequest {

    // Coordenadas donde está la empresa.
    private double latitud;
    private double longitud;

    // Distancia máxima a la que se puede fichar desde esas coordenadas.
    private double radio;

    // El ID de la empresa que estamos configurando.
    private Integer empresa_id;

    public ConfigUbicacionRequest(double latitud, double longitud, double radio, Integer empresa_id) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.radio = radio;
        this.empresa_id = empresa_id;
    }
}