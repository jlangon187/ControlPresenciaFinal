package com.example.controlpresencia.data.model;

// Modelo que representa el horario de un día concreto para un trabajador.
// Contiene la información sobre el día de la semana, las horas de entrada y salida, y si es un día libre.
public class DiaHorario {

    // Identificador numérico del día de la semana (1 para Lunes, 2 para Martes, etc.).
    private int idDia;

    // Nombre del día de la semana (por ejemplo, "Lunes", "Martes").
    private String nombreDia;

    // Hora de entrada programada para este día, en formato de texto.
    private String horaEntrada;

    // Hora de salida programada para este día, en formato de texto.
    private String horaSalida;

    // Indicador booleano que es 'true' si este día es un día libre.
    private boolean esLibre;

    public int getIdDia() { return idDia; }
    public String getNombreDia() { return nombreDia; }
    public String getHoraEntrada() { return horaEntrada; }
    public String getHoraSalida() { return horaSalida; }
    public boolean isEsLibre() { return esLibre; }
}