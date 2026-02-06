package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("access_token")
    private String accessToken;

    // Getter
    public String getAccessToken() {
        return accessToken;
    }
}