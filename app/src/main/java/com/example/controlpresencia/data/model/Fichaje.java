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
    private String fecha;
    @SerializedName("hora_entrada")
    private String horaEntrada;
    @SerializedName("hora_salida")
    private String horaSalida;

    @SerializedName("latitud")
    private Double latitud;

    @SerializedName("longitud")
    private Double longitud;

    @SerializedName("horas_extra")
    private double horasExtra;


    public String getHoraEntradaFormateada() {
        return formatearHora(horaEntrada);
    }

    public String getHoraSalidaFormateada() {
        if (horaSalida == null) return "En curso";
        return formatearHora(horaSalida);
    }

    public String getTotalHoras() {
        if (horaSalida == null) return "En curso";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date dateEntrada = sdf.parse(horaEntrada);
            Date dateSalida = sdf.parse(horaSalida);

            long diff = dateSalida.getTime() - dateEntrada.getTime();
            long horas = TimeUnit.MILLISECONDS.toHours(diff);
            long minutos = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

            String totalTrabajado = String.format(Locale.getDefault(), "%dh %02dm", horas, minutos);

            if (horasExtra > 0) {
                return totalTrabajado + " | Extra: +" + horasExtra + "h";
            }
            return totalTrabajado;

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

    public String getFecha() { return fecha; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
}