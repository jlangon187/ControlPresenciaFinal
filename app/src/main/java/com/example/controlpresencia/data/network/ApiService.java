package com.example.controlpresencia.data.network;

import com.example.controlpresencia.data.model.Empresa;
import com.example.controlpresencia.data.model.Fichaje;
import com.example.controlpresencia.data.model.IncidenciaRequest;
import com.example.controlpresencia.data.model.LoginResponse;
import com.example.controlpresencia.data.model.FichajeRequest;
import com.example.controlpresencia.data.model.LoginRequest;
import com.example.controlpresencia.data.model.ResetPasswordRequest;
import com.example.controlpresencia.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @POST("api/incidencias")
    Call<Void> crearIncidencia(@Header("Authorization") String token, @Body IncidenciaRequest incidencia);

    @POST("api/reset-password-request")
    Call<Void> solicitarResetPassword(@Body ResetPasswordRequest request);

    @GET("api/admin/empleados")
    Call<List<User>> getEmpleadosAdmin(@Header("Authorization") String token);

    @GET("api/admin/registros/{id}")
    Call<List<Fichaje>> getHistorialAdmin(@Header("Authorization") String token, @Path("id") int idTrabajador);

    @GET("api/admin/empresas")
    Call<List<Empresa>> getEmpresasAdmin(@Header("Authorization") String token);

    @GET("api/admin/empleados")
    Call<List<User>> getEmpleadosAdmin(@Header("Authorization") String token, @Query("empresa_id") Integer empresaId);

    @POST("api/fichaje-nfc")
    Call<FichajeResponse> ficharNFC(
            @Header("Authorization") String token,
            @Body FichajeRequest request
    );
}