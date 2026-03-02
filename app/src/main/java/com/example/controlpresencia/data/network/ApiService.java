package com.example.controlpresencia.data.network;

import com.example.controlpresencia.data.model.AdminStatsResponse;
import com.example.controlpresencia.data.model.ConfigUbicacionRequest;
import com.example.controlpresencia.data.model.DiaHorario;
import com.example.controlpresencia.data.model.Empresa;
import com.example.controlpresencia.data.model.Fichaje;
import com.example.controlpresencia.data.model.FichajeResponse;
import com.example.controlpresencia.data.model.Incidencia;
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

// Aquí definimos todas las llamadas que hacemos al servidor (la API).
public interface ApiService {

    // Para loguearse con email y contraseña.
    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Para fichar la entrada usando el GPS.
    @POST("api/fichar/entrada")
    Call<Void> ficharEntrada(
            @Header("Authorization") String token,
            @Body FichajeRequest request
    );

    // Para fichar la salida.
    @POST("api/fichar/salida")
    Call<Void> ficharSalida(@Header("Authorization") String token);

    // Carga los datos del usuario que está usando la app.
    @GET("api/perfil")
    Call<User> getPerfil(@Header("Authorization") String token);

    // Trae la lista de todos los fichajes que ha hecho el usuario.
    @GET("api/fichajes")
    Call<List<Fichaje>> getHistorial(@Header("Authorization") String token);

    // Para enviar una nueva incidencia al jefe.
    @POST("api/incidencias")
    Call<Void> crearIncidencia(@Header("Authorization") String token, @Body IncidenciaRequest incidencia);

    // Para pedir que te manden un correo y cambiar la contraseña si se te olvida.
    @POST("api/reset-password-request")
    Call<Void> solicitarResetPassword(@Body ResetPasswordRequest request);

    // El administrador lo usa para ver la lista de todos los trabajadores.
    @GET("api/admin/empleados")
    Call<List<User>> getEmpleadosAdmin(@Header("Authorization") String token);

    // El administrador lo usa para ver los fichajes de un trabajador concreto.
    @GET("api/admin/registros/{id}")
    Call<List<Fichaje>> getHistorialAdmin(@Header("Authorization") String token, @Path("id") int idTrabajador);

    // Lista las empresas para el administrador.
    @GET("api/admin/empresas")
    Call<List<Empresa>> getEmpresasAdmin(@Header("Authorization") String token);

    // Lista empleados filtrando por empresa.
    @GET("api/admin/empleados")
    Call<List<User>> getEmpleadosAdmin(@Header("Authorization") String token, @Query("empresa_id") Integer empresaId);

    // Para fichar usando la tarjeta o etiqueta NFC.
    @POST("api/fichaje-nfc")
    Call<FichajeResponse> ficharNFC(
            @Header("Authorization") String token,
            @Body FichajeRequest request
    );

    // Trae las incidencias reportadas en una empresa específica.
    @GET("api/incidencias/empresa/{id}")
    Call<List<Incidencia>> getIncidenciasEmpresa(
            @Header("Authorization") String token,
            @Path("id") int idEmpresa
    );

    // Trae el horario que tiene asignado el usuario para la semana.
    @GET("api/horario/mi-horario")
    Call<List<DiaHorario>> getMiHorario(@Header("Authorization") String token);

    // Estadísticas rápidas para el panel del administrador.
    @GET("/api/admin/stats")
    Call<AdminStatsResponse> getAdminStats(@Header("Authorization") String token, @Query("empresa_id") Integer empresaId);

    // Para que el admin guarde las coordenadas y el radio de la oficina.
    @POST("/api/admin/configurar-ubicacion")
    Call<FichajeResponse> configurarUbicacionEmpresa(@Header("Authorization") String token, @Body ConfigUbicacionRequest request);
}