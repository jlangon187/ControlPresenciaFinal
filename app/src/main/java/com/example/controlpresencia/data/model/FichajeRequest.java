package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

public class FichajeRequest {
    private Double latitud;
    private Double longitud;

    @SerializedName("nfc_uid")
    private String nfcUid;

    public FichajeRequest(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public FichajeRequest(String nfcUid) {
        this.nfcUid = nfcUid;
    }

    // Getters y Setters
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getNfcUid() { return nfcUid; }
    public void setNfcUid(String nfcUid) { this.nfcUid = nfcUid; }
}