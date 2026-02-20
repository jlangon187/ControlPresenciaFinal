package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

public class Empresa {
    private String nombrecomercial;
    private Double latitud;
    private Double longitud;
    private Integer radio;
    @SerializedName("id_empresa")
    private int idEmpresa;

    // Getters
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public Integer getRadio() { return radio; }
    public String getNombre() { return nombrecomercial; }
    public int getIdEmpresa() { return idEmpresa; }
}