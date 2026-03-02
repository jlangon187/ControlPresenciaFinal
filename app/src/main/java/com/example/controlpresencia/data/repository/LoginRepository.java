package com.example.controlpresencia.data.repository;

import com.example.controlpresencia.data.model.LoginRequest;
import com.example.controlpresencia.data.model.LoginResponse;
import com.example.controlpresencia.data.network.ApiService;
import com.example.controlpresencia.data.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Esta clase gestiona todo lo relacionado con el inicio de sesión del usuario.
public class LoginRepository {

    private ApiService apiService;

    public LoginRepository() {
        // Pillamos la instancia de la API.
        apiService = RetrofitClient.getInstance().getMyApi();
    }

    // Interfaz para avisar si el login ha ido bien o no.
    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String message);
    }

    // Método para mandarle el email, la contraseña y el token de notificaciones al servidor para entrar.
    public void hacerLogin(String email, String password, String fcmToken, LoginCallback callback) {
        LoginRequest request = new LoginRequest(email, password, fcmToken);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Si el servidor acepta el login, nos devuelve el token y los datos del usuario.
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    // Si falla, avisamos de que los datos no son correctos.
                    callback.onError("Credenciales incorrectas");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Si ni siquiera hay conexión.
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}