package com.example.controlpresencia.data.model;

// Solo sirve para pasarle el correo al servidor cuando alguien se olvida la contraseña.
public class ResetPasswordRequest {
    // El email de la cuenta que queremos recuperar.
    private String email;

    public ResetPasswordRequest(String email) {
        this.email = email;
    }
}