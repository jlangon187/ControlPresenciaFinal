package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    private String email;
    private String password;
    @SerializedName("fcm_token")
    private String fcmToken;

    public LoginRequest(String email, String password, String fcmToken) {
        this.email = email;
        this.password = password;
        this.fcmToken = fcmToken;
    }

    // Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}