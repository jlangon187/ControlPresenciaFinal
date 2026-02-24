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

    @SerializedName("turno_teorico")
    private String turnoTeorico;


    public String getHoraEntradaFormateada() {
        String hora = formatearHora(horaEntrada);

        if (latitud == null || longitud == null) {
            return hora + " 💳 (NFC)";
        } else {
            return hora + " 📍 (GPS)";
        }
    }

    public String getHoraSalidaFormateada() {
        if (horaSalida == null) return "En curso";
        return formatearHora(horaSalida);
    }

    public String getTotalHoras() {
        if (horaSalida == null) return "⏱️ Turno Abierto";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date dateEntrada = sdf.parse(horaEntrada);
            Date dateSalida = sdf.parse(horaSalida);

            long diff = dateSalida.getTime() - dateEntrada.getTime();
            long horasTrabajadas = TimeUnit.MILLISECONDS.toHours(diff);
            long minutosTrabajados = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
            long totalMinutosReales = TimeUnit.MILLISECONDS.toMinutes(diff);

            String totalTrabajado = String.format(Locale.getDefault(), "%dh %02dm", horasTrabajadas, minutosTrabajados);

            if (horasExtra > 0) {
                long extraMinutosTotales = Math.round(horasExtra * 60);
                long hExtra = extraMinutosTotales / 60;
                long mExtra = extraMinutosTotales % 60;
                String formatoExtra = String.format(Locale.getDefault(), "%dh %02dm", hExtra, mExtra);
                return "Total: " + totalTrabajado + " | 🔥 Extra: +" + formatoExtra;
            }

            long totalMinutosTeoricos = 0;
            if (turnoTeorico != null && turnoTeorico.contains(" a ")) {
                try {
                    String[] partes = turnoTeorico.split(" a ");
                    SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date hE = sdfHora.parse(partes[0].trim());
                    Date hS = sdfHora.parse(partes[1].trim());

                    long diffTeorica = hS.getTime() - hE.getTime();
                    if (diffTeorica < 0) diffTeorica += TimeUnit.DAYS.toMillis(1); // Turnos nocturnos
                    totalMinutosTeoricos = TimeUnit.MILLISECONDS.toMinutes(diffTeorica);
                } catch (Exception ignored) {}
            }

            if (totalMinutosTeoricos > 0 && totalMinutosReales < (totalMinutosTeoricos - 6)) {
                long faltanMinutos = totalMinutosTeoricos - totalMinutosReales;
                long fH = faltanMinutos / 60;
                long fM = faltanMinutos % 60;
                String formatoFalta = String.format(Locale.getDefault(), "%dh %02dm", fH, fM);
                return "Total: " + totalTrabajado + " | ⚠️ Faltan: " + formatoFalta;
            }

            return "Total: " + totalTrabajado + " | ✔️ Cumplido";

        } catch (Exception e) {
            return "Error al calcular horas";
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
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdfInput.parse(fechaIso);

            SimpleDateFormat sdfOutput = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdfOutput.format(date);
        } catch (ParseException e) {
            return "--:--";
        }
    }

    public String getTurnoTeorico() {
        if (turnoTeorico == null || turnoTeorico.equals("Sin horario") || turnoTeorico.equals("Fuera de turno")) {
            return "";
        }
        return " | 🕒 " + turnoTeorico;
    }

    public String getFecha() { return fecha; }
    public Double getLatitud() { return latitud; }
    public Double getLongitud() { return longitud; }
    public double getHorasExtra() {
        return horasExtra;
    }
}