package com.example.controlpresencia.data.network;

import com.example.controlpresencia.data.model.Fichaje;
import com.example.controlpresencia.data.model.LoginResponse;
import com.example.controlpresencia.data.model.FichajeRequest;
import com.example.controlpresencia.data.model.LoginRequest;
import com.example.controlpresencia.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/fichar/entrada")
    Call<Void> ficharEntrada(
            @Header("Authorization") String token,
            @Body FichajeRequest request
    );

    @POST("api/fichar/salida")
    Call<Void> ficharSalida(@Header("Authorization") String token);

    @GET("api/perfil")
    Call<User> getPerfil(@Header("Authorization") String token);

    @GET("api/fichajes")
    Call<List<Fichaje>> getHistorial(@Header("Authorization") String token);
}