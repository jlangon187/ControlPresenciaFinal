package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

// Clase que representa la solicitud para realizar un fichaje, ya sea por GPS o por NFC.
public class FichajeRequest {
    // Latitud de la ubicación del fichaje (si se usa GPS). Puede ser nulo.
    private Double latitud;
    // Longitud de la ubicación del fichaje (si se usa GPS). Puede ser nulo.
    private Double longitud;

    // Identificador único de la tarjeta NFC utilizada para el fichaje (si se usa NFC). Puede ser nulo.
    @SerializedName("nfc_uid")
    private String nfcUid;

    // Constructor para un fichaje basado en coordenadas GPS.
    public FichajeRequest(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    // Constructor para un fichaje basado en un UID de NFC.
    public FichajeRequest(String nfcUid) {
        this.nfcUid = nfcUid;
    }

    // Getters y Setters (omitidos según la instrucción)
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getNfcUid() { return nfcUid; }
    public void setNfcUid(String nfcUid) { this.nfcUid = nfcUid; }
}