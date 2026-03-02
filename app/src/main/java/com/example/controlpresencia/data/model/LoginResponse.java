package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

// Clase que representa la respuesta del servidor al intentar iniciar sesión.
// Contiene el token de acceso, el rol del usuario y su nombre.
public class LoginResponse {

    // Token de acceso que se utiliza para autenticar futuras peticiones a la API.
    @SerializedName("access_token")
    private String accessToken;

    // Rol del usuario que ha iniciado sesión (por ejemplo, "administrador", "empleado").
    @SerializedName("rol")
    private String rol;

    // Nombre del usuario que ha iniciado sesión.
    @SerializedName("nombre")
    private String nombre;

    // Getters (omitidos según la instrucción)
    public String getAccessToken() { return accessToken; }
    public String getRol() { return rol; }
    public String getNombre() { return nombre; }
}