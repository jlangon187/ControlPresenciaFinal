package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("rol")
    private String rol;

    @SerializedName("nombre")
    private String nombre;

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRol() { return rol; }
    public String getNombre() { return nombre; }
}