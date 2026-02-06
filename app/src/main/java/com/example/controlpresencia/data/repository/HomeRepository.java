package com.example.controlpresencia.data.repository;

import com.example.controlpresencia.data.model.FichajeRequest;
import com.example.controlpresencia.data.network.ApiService;
import com.example.controlpresencia.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {
    private ApiService apiService;

    public HomeRepository() {
        apiService = RetrofitClient.getInstance().getMyApi();
    }

    public interface FichajeCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Fichar Entrada
    public void registrarEntrada(String token, double lat, double lon, FichajeCallback callback) {
        FichajeRequest request = new FichajeRequest(lat, lon);

        apiService.ficharEntrada(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("✅ Entrada registrada correctamente");
                } else if (response.code() == 403) {
                    callback.onError("⛔ Estás demasiado lejos de la empresa.");
                } else if (response.code() == 409) {
                    callback.onError("⚠️ Ya tienes un turno abierto.");
                } else {
                    callback.onError("Error: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Error de conexión");
            }
        });
    }

    // Fichar Salida
    public void registrarSalida(String token, FichajeCallback callback) {
        apiService.ficharSalida(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("👋 Salida registrada. ¡Hasta mañana!");
                } else if (response.code() == 409) {
                    // AQUÍ ESTÁ EL CAMBIO: Personalizamos el mensaje
                    callback.onError("⚠️ No tienes ningún turno abierto para cerrar.");
                } else if (response.code() == 401) {
                    callback.onError("🔒 Sesión caducada. Vuelve a hacer login.");
                } else {
                    callback.onError("Error desconocido: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Error de conexión. Revisa tu internet.");
            }
        });
    }
}