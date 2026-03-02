package com.example.controlpresencia.data.model;

// Esta clase es el paquetito de datos que mandamos al servidor para crear una incidencia.
public class IncidenciaRequest {
    // El resumen del problema.
    private String titulo;
    // Todo el detalle de lo que ha pasado.
    private String descripcion;

    public IncidenciaRequest(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }
}