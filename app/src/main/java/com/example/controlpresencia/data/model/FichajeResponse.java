package com.example.controlpresencia.data.model;

// Esta clase es para pillar el mensaje que nos devuelve el servidor cuando fichamos.
public class FichajeResponse {
    // El texto que dice si el fichaje ha ido bien o qué ha pasado.
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}