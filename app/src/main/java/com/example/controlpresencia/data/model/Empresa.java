package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

// Modelo que representa una empresa recibida desde la API.
// Contiene la información básica de la empresa, incluyendo su ubicación y el radio permitido para los fichajes.
public class Empresa {

    // Nombre comercial de la empresa.
    private String nombrecomercial;

    // Coordenadas geográficas de la empresa (latitud).
    private Double latitud;
    // Coordenadas geográficas de la empresa (longitud).
    private Double longitud;

    // Radio en metros alrededor de la ubicación de la empresa dentro del cual se permiten los fichajes.
    private Integer radio;

    // Identificador único de la empresa.
    @SerializedName("id_empresa")
    private int idEmpresa;

    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public Integer getRadio() { return radio; }
    public String getNombre() { return nombrecomercial; }
    public int getIdEmpresa() { return idEmpresa; }
}