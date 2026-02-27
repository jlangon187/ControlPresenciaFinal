package com.example.controlpresencia.data.model;

public class DiaHorario {
    private int idDia; // 1=Lunes, 2=Martes...
    private String nombreDia;
    private String horaEntrada;
    private String horaSalida;
    private boolean esLibre;

    // Getters
    public int getIdDia() { return idDia; }
    public String getNombreDia() { return nombreDia; }
    public String getHoraEntrada() { return horaEntrada; }
    public String getHoraSalida() { return horaSalida; }
    public boolean isEsLibre() { return esLibre; }
}