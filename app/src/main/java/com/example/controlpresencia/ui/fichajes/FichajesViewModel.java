package com.example.controlpresencia.ui.fichajes;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.controlpresencia.data.model.Fichaje;
import com.example.controlpresencia.data.network.RetrofitClient;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// ViewModel para la pantalla de historial de fichajes. Gestiona la carga, el filtrado y el cálculo de horas.
public class FichajesViewModel extends ViewModel {
    // Guardamos la lista completa que viene del servidor para poder filtrar sin volver a llamar a la API.
    private List<Fichaje> listaCompleta = new ArrayList<>();
    private MutableLiveData<List<Fichaje>> listaFichajes = new MutableLiveData<>();
    private MutableLiveData<String> resumenTrabajado = new MutableLiveData<>();
    private MutableLiveData<String> resumenExtra = new MutableLiveData<>();
    private MutableLiveData<String> errorMsg = new MutableLiveData<>();

    public MutableLiveData<List<Fichaje>> getListaFichajes() { return listaFichajes; }
    public MutableLiveData<String> getResumenTrabajado() { return resumenTrabajado; }
    public MutableLiveData<String> getResumenExtra() { return resumenExtra; }

    // Pide al servidor todos los fichajes que ha hecho el usuario logueado.
    public void cargarHistorial(String token) {
        RetrofitClient.getInstance().getMyApi().getHistorial(token).enqueue(new Callback<List<Fichaje>>() {
            @Override
            public void onResponse(Call<List<Fichaje>> call, Response<List<Fichaje>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta = response.body();
                    listaFichajes.setValue(listaCompleta);
                    // Calculamos los totales una vez recibimos la lista.
                    calcularTotales(listaCompleta);
                } else {
                    errorMsg.setValue("Error al cargar datos");
                }
            }
            @Override
            public void onFailure(Call<List<Fichaje>> call, Throwable t) {
                errorMsg.setValue("Fallo de conexión");
            }
        });
    }

    // Suma todos los minutos trabajados y las horas extra de la lista que se esté viendo.
    private void calcularTotales(List<Fichaje> datos) {
        long totalMinutos = 0;
        double totalExtrasDec = 0.0;

        for (Fichaje f : datos) {
            totalMinutos += f.getMinutosTrabajados();
            totalExtrasDec += f.getHorasExtra();
        }

        // Formateamos los minutos totales a formato "Xh Ym".
        long horas = TimeUnit.MINUTES.toHours(totalMinutos);
        long minutos = totalMinutos % 60;
        resumenTrabajado.setValue(horas + "h " + minutos + "m");

        // Hacemos lo mismo para las horas extra.
        long extraMinutosTotales = Math.round(totalExtrasDec * 60);
        long hExtra = extraMinutosTotales / 60;
        long mExtra = extraMinutosTotales % 60;

        if (extraMinutosTotales > 0) {
            resumenExtra.setValue("+" + hExtra + "h " + String.format(Locale.getDefault(), "%02d", mExtra) + "m");
        } else {
            resumenExtra.setValue("0h 00m");
        }
    }

    // Filtra la lista de fichajes según un rango de fechas (long de milisegundos).
    public void filtrarPorRango(Long fechaInicio, Long fechaFin) {
        // Si no hay fechas, mostramos todo.
        if (fechaInicio == null || fechaFin == null) {
            listaFichajes.setValue(listaCompleta);
            calcularTotales(listaCompleta);
            return;
        }

        List<Fichaje> listaFiltrada = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        for (Fichaje f : listaCompleta) {
            try {
                Date dateFichaje = sdf.parse(f.getFecha());
                long timeFichaje = dateFichaje.getTime();
                // Si el fichaje entra en el rango, lo añadimos a la nueva lista.
                if (timeFichaje >= fechaInicio && timeFichaje <= fechaFin) {
                    listaFiltrada.add(f);
                }
            } catch (Exception ignored) {}
        }

        // Actualizamos lo que ve el usuario y los totales del resumen.
        listaFichajes.setValue(listaFiltrada);
        calcularTotales(listaFiltrada);
    }
}