package com.example.controlpresencia.data.model;

// Esta clase sirve para guardar las estadísticas que ve el administrador.
public class AdminStatsResponse {

    // Empleados que están trabajando ahora mismo.
    private int activos;

    // Empleados que no han venido o no han fichado.
    private int ausencias;

    public int getActivos() { return activos; }
    public int getAusencias() { return ausencias; }
}