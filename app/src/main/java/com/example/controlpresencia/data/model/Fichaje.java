package com.example.controlpresencia.data.model;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// Clase que representa un registro de fichaje de entrada y salida de un trabajador.
// Contiene la información detallada de cada fichaje, incluyendo horas, ubicación y cálculo de horas trabajadas.
public class Fichaje {
    // Identificador único del registro de fichaje.
    @SerializedName("id_registro")
    private int id;
    // Fecha en la que se realizó el fichaje.
    private String fecha;
    // Hora de entrada del trabajador.
    @SerializedName("hora_entrada")
    private String horaEntrada;
    // Hora de salida del trabajador. Puede ser nula si el turno aún está abierto.
    @SerializedName("hora_salida")
    private String horaSalida;

    // Latitud donde se realizó el fichaje (si se usó GPS).
    @SerializedName("latitud")
    private Double latitud;

    // Longitud donde se realizó el fichaje (si se usó GPS).
    @SerializedName("longitud")
    private Double longitud;

    // Horas extra acumuladas en este fichaje.
    @SerializedName("horas_extra")
    private double horasExtra;

    // Turno teórico asignado al trabajador para ese día.
    @SerializedName("turno_teorico")
    private String turnoTeorico;


    // Formatea la hora de entrada y añade un icono indicando si fue por GPS o NFC.
    public String getHoraEntradaFormateada() {
        String hora = formatearHora(horaEntrada);

        if (latitud == null || longitud == null) {
            return hora + " 💳 (NFC)"; // Fichaje realizado con NFC
        } else {
            return hora + " 📍 (GPS)"; // Fichaje realizado con GPS
        }
    }

    // Formatea la hora de salida. Si el turno aún está abierto, devuelve "En curso".
    public String getHoraSalidaFormateada() {
        if (horaSalida == null) return "En curso";
        return formatearHora(horaSalida);
    }

    // Calcula el total de horas trabajadas en el fichaje y muestra si hay horas extra o si faltan horas.
    public String getTotalHoras() {
        if (horaSalida == null) return "⏱️ Turno Abierto"; // El turno aún no ha terminado.

        try {
            // Formato para parsear las horas de entrada y salida.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date dateEntrada = sdf.parse(horaEntrada);
            Date dateSalida = sdf.parse(horaSalida);

            // Calcula la diferencia en milisegundos.
            long diff = dateSalida.getTime() - dateEntrada.getTime();
            long horasTrabajadas = TimeUnit.MILLISECONDS.toHours(diff);
            long minutosTrabajados = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
            long totalMinutosReales = TimeUnit.MILLISECONDS.toMinutes(diff);

            // Formatea el tiempo total trabajado.
            String totalTrabajado = String.format(Locale.getDefault(), "%dh %02dm", horasTrabajadas, minutosTrabajados);

            // Si hay horas extra, las calcula y las añade al resumen.
            if (horasExtra > 0) {
                long extraMinutosTotales = Math.round(horasExtra * 60);
                long hExtra = extraMinutosTotales / 60;
                long mExtra = extraMinutosTotales % 60;
                String formatoExtra = String.format(Locale.getDefault(), "%dh %02dm", hExtra, mExtra);
                return "Total: " + totalTrabajado + " | Extra: +" + formatoExtra;
            }

            long totalMinutosTeoricos = 0;
            // Intenta calcular los minutos teóricos del turno si está disponible.
            if (turnoTeorico != null && turnoTeorico.contains(" a ")) {
                try {
                    String[] partes = turnoTeorico.split(" a ");
                    SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date hE = sdfHora.parse(partes[0].trim());
                    Date hS = sdfHora.parse(partes[1].trim());

                    long diffTeorica = hS.getTime() - hE.getTime();
                    if (diffTeorica < 0) diffTeorica += TimeUnit.DAYS.toMillis(1); // Manejo de turnos nocturnos que pasan de día.
                    totalMinutosTeoricos = TimeUnit.MILLISECONDS.toMinutes(diffTeorica);
                } catch (Exception ignored) {} // Ignora errores si el formato del turno teórico es incorrecto.
            }

            // Si se trabajó menos de lo esperado según el turno teórico, indica cuántas horas faltan.
            if (totalMinutosTeoricos > 0 && totalMinutosReales < (totalMinutosTeoricos - 6)) { // Tolerancia de 6 minutos.
                long faltanMinutos = totalMinutosTeoricos - totalMinutosReales;
                long fH = faltanMinutos / 60;
                long fM = faltanMinutos % 60;
                String formatoFalta = String.format(Locale.getDefault(), "%dh %02dm", fH, fM);
                return "Total: " + totalTrabajado + " | ⚠️ Faltan: " + formatoFalta;
            }

            // Si no hay horas extra y se cumplió el horario, indica "Cumplido".
            return "Total: " + totalTrabajado + " | ✔️ Cumplido";

        } catch (Exception e) {
            // En caso de error al parsear fechas, devuelve un mensaje de error.
            return "Error al calcular horas";
        }
    }

    // Calcula y devuelve los minutos totales trabajados en el fichaje.
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

    // Método privado para formatear una cadena de fecha ISO a formato HH:mm.
    private String formatearHora(String fechaIso) {
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdfInput.parse(fechaIso);

            SimpleDateFormat sdfOutput = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdfOutput.format(date);
        } catch (ParseException e) {
            return "--:--"; // Devuelve "--:--" si hay un error al parsear.
        }
    }

    // Devuelve el turno teórico formateado, o una cadena vacía si no hay turno asignado o es "Fuera de turno".
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