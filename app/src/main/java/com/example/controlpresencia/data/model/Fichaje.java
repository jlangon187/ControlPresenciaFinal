package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Fichaje {
    @SerializedName("id_registro")
    private int id;
    private String fecha; // Formato YYYY-MM-DD
    @SerializedName("hora_entrada")
    private String horaEntrada; // Formato ISO
    @SerializedName("hora_salida")
    private String horaSalida; // Formato ISO

    // --- CÁLCULOS AUXILIARES ---

    public String getHoraEntradaFormateada() {
        return formatearHora(horaEntrada);
    }

    public String getHoraSalidaFormateada() {
        if (horaSalida == null) return "En curso";
        return formatearHora(horaSalida);
    }

    public String getTotalHoras() {
        if (horaSalida == null) return "-";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date dateEntrada = sdf.parse(horaEntrada);
            Date dateSalida = sdf.parse(horaSalida);

            long diff = dateSalida.getTime() - dateEntrada.getTime();
            long horas = TimeUnit.MILLISECONDS.toHours(diff);
            long minutos = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

            return String.format(Locale.getDefault(), "%dh %02dm", horas, minutos);
        } catch (Exception e) {
            return "Error";
        }
    }

    public long getMinutosTrabajados() {
        if (horaSalida == null) return 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date dateEntrada = sdf.parse(horaEntrada);
            Date dateSalida = sdf.parse(horaSalida);
            long diff = dateSalida.getTime() - dateEntrada.getTime();
            return TimeUnit.MILLISECONDS.toMinutes(diff);
        } catch (Exception e) { return 0; }
    }

    private String formatearHora(String fechaIso) {
        try {
            // La API devuelve: 2026-02-03T08:00:00
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdfInput.parse(fechaIso);

            // Queremos mostrar: 08:00
            SimpleDateFormat sdfOutput = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdfOutput.format(date);
        } catch (ParseException e) {
            return "--:--";
        }
    }

    // Getter simple para la fecha
    public String getFecha() { return fecha; }
}