package com.example.controlpresencia.data.network;

import com.example.controlpresencia.data.network.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Clase para configurar la conexión con el servidor.
// Se usa el patrón Singleton para no estar creando conexiones nuevas todo el rato.
public class RetrofitClient {

    // URL base de mi servidor en PythonAnywhere.
    private static final String BASE_URL = "https://javiliyors.eu.pythonanywhere.com/";
    private static RetrofitClient instance = null;
    private ApiService myApi;

    private RetrofitClient() {
        // Configuramos un interceptor para que se vean todas las peticiones y respuestas en la consola de Android Studio (Logcat).
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        // Aquí se crea el objeto Retrofit con la URL base y el convertidor de JSON (Gson).
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        myApi = retrofit.create(ApiService.class);
    }

    // Método para pillar la instancia única de esta clase.
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getMyApi() {
        return myApi;
    }
}