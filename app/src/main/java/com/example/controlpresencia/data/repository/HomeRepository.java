package com.example.controlpresencia.data.repository;

import com.example.controlpresencia.data.model.FichajeRequest;
import com.example.controlpresencia.data.network.ApiService;
import com.example.controlpresencia.data.network.RetrofitClient;
import org.json.JSONObject;

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

    private String extraerMensajeError(Response<?> response, String mensajePorDefecto) {
        try {
            if (response.errorBody() != null) {
                String errorStr = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorStr);
                if (jsonObject.has("message")) {
                    return jsonObject.getString("message");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mensajePorDefecto;
    }

    // Fichar Entrada
    public void registrarEntrada(String token, double lat, double lon, FichajeCallback callback) {
        FichajeRequest request = new FichajeRequest(lat, lon);

        apiService.ficharEntrada(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("✅ Entrada registrada correctamente");
                } else if (response.code() == 403 || response.code() == 409) {
                    String motivoReal = extraerMensajeError(response, "Error al fichar.");
                    callback.onError("⚠️ " + motivoReal);
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Error de conexión. Revisa tu internet.");
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
                    String motivoReal = extraerMensajeError(response, "No tienes turno abierto.");
                    callback.onError("⚠️ " + motivoReal);
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