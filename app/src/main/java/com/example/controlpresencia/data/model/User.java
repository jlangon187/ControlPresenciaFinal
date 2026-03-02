package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

// Clase que representa a un usuario o trabajador en el sistema.
// Se usa para almacenar la información personal y de la empresa del trabajador.
public class User {

    // Identificador único del trabajador.
    @SerializedName("id_trabajador")
    private int idTrabajador;

    // Nombre del trabajador.
    @SerializedName("nombre")
    private String nombre;

    // Apellidos del trabajador.
    @SerializedName("apellidos")
    private String apellidos;

    // Correo electrónico del trabajador, usado para login y notificaciones.
    @SerializedName("email")
    private String email;

    // Objeto que representa la empresa a la que pertenece el trabajador.
    @SerializedName("empresa")
    private Empresa empresa;

    // Getters
    public int getIdTrabajador() { return idTrabajador; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public Empresa getEmpresa() { return empresa; }
    // Método que devuelve el nombre completo del trabajador, combinando nombre y apellidos.
    public String getNombreCompleto() {
        return nombre + " " + (apellidos != null ? apellidos : "");
    }
}