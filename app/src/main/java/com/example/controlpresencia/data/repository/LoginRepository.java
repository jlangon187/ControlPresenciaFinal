package com.example.controlpresencia.data.repository;

import com.example.controlpresencia.data.model.LoginRequest;
import com.example.controlpresencia.data.model.LoginResponse;
import com.example.controlpresencia.data.network.ApiService;
import com.example.controlpresencia.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRepository {

    private ApiService apiService;

    public LoginRepository() {
        // Obtenemos la instancia de la API
        apiService = RetrofitClient.getInstance().getMyApi();
    }

    // Interfaz para devolver el resultado al ViewModel
    public interface LoginCallback {
        void onSuccess(String token);
        void onError(String message);
    }

    public void hacerLogin(String email, String password, LoginCallback callback) {
        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ¡Login correcto! Devolvemos el token
                    callback.onSuccess(response.body().getAccessToken());
                } else {
                    // Login fallido (401, etc)
                    callback.onError("Credenciales incorrectas");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Error de red (sin internet, servidor caído)
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}