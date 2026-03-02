package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

// Clase que representa la solicitud de inicio de sesión de un usuario.
// Contiene las credenciales (email y contraseña) y el token de FCM para notificaciones.
public class LoginRequest {
    // El email del usuario para iniciar sesión.
    private String email;
    // La contraseña del usuario para iniciar sesión.
    private String password;
    // Token de Firebase Cloud Messaging para enviar notificaciones push a este dispositivo.
    @SerializedName("fcm_token")
    private String fcmToken;

    // Constructor para crear una nueva solicitud de login.
    public LoginRequest(String email, String password, String fcmToken) {
        this.email = email;
        this.password = password;
        this.fcmToken = fcmToken;
    }

    // Getters y Setters (omitidos según la instrucción)
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}