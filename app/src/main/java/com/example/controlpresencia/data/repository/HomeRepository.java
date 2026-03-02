package com.example.controlpresencia.data.repository;

import com.example.controlpresencia.data.model.FichajeRequest;
import com.example.controlpresencia.data.network.ApiService;
import com.example.controlpresencia.data.network.RetrofitClient;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Esta clase se encarga de gestionar los datos de la pantalla principal (Home).
// Básicamente, aquí es donde llamamos a la API para registrar las entradas y salidas.
public class HomeRepository {
    private ApiService apiService;

    public HomeRepository() {
        // Pillamos la instancia de la API para poder usarla.
        apiService = RetrofitClient.getInstance().getMyApi();
    }

    // Interfaz para avisar a la pantalla si el fichaje ha ido bien o mal.
    public interface FichajeCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Método para sacar el mensaje de error que manda el servidor en el JSON.
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

    // Registra la entrada del trabajador enviando su ubicación actual.
    public void registrarEntrada(String token, double lat, double lon, FichajeCallback callback) {
        FichajeRequest request = new FichajeRequest(lat, lon);

        apiService.ficharEntrada(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("✅ Entrada registrada correctamente");
                } else if (response.code() == 403 || response.code() == 409) {
                    // Si el servidor da un error de "prohibido" o "conflicto" (ej. ya has fichado o estás lejos).
                    String motivoReal = extraerMensajeError(response, "Error al fichar.");
                    callback.onError("⚠️ " + motivoReal);
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Si falla la conexión a internet.
                callback.onError("Error de conexión. Revisa tu internet.");
            }
        });
    }

    // Registra la salida del trabajador.
    public void registrarSalida(String token, FichajeCallback callback) {
        apiService.ficharSalida(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("👋 Salida registrada. ¡Hasta mañana!");
                } else if (response.code() == 409) {
                    // Error si intentas salir sin haber entrado antes.
                    String motivoReal = extraerMensajeError(response, "No tienes turno abierto.");
                    callback.onError("⚠️ " + motivoReal);
                } else if (response.code() == 401) {
                    // Si el token ya no vale.
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