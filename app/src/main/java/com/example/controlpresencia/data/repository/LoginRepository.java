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
        apiService = RetrofitClient.getInstance().getMyApi();
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse response); // <--- CAMBIO CLAVE
        void onError(String message);
    }

    public void hacerLogin(String email, String password, String fcmToken, LoginCallback callback) {
        LoginRequest request = new LoginRequest(email, password, fcmToken);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body()); // <--- Pasamos todo el objeto
                } else {
                    callback.onError("Credenciales incorrectas");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}