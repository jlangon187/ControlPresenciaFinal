package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;

// Clase para guardar los datos de una incidencia reportada por un empleado.
public class Incidencia {

    // ID único de la incidencia.
    @SerializedName("id")
    private int id;

    // Título corto que resume el problema.
    @SerializedName("titulo")
    private String titulo;

    // Explicación detallada de lo que ha pasado.
    @SerializedName("descripcion")
    private String descripcion;

    // Cuándo se creó la incidencia.
    @SerializedName("fecha_hora")
    private String fecha;

    // Estado actual (pendiente, resuelta, etc).
    @SerializedName("estado")
    private String estado;

    // Nombre del empleado que ha puesto la incidencia.
    @SerializedName("empleado_nombre")
    private String empleadoNombre;

    public Incidencia() {
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEmpleadoNombre() { return empleadoNombre; }
    public void setEmpleadoNombre(String empleadoNombre) { this.empleadoNombre = empleadoNombre; }
}