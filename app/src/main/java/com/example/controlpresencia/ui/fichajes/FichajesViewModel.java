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

public class FichajesViewModel extends ViewModel {
    private List<Fichaje> listaCompleta = new ArrayList<>();
    private MutableLiveData<List<Fichaje>> listaFichajes = new MutableLiveData<>();
    private MutableLiveData<String> resumenSemanal = new MutableLiveData<>();
    private MutableLiveData<String> errorMsg = new MutableLiveData<>();
    public MutableLiveData<List<Fichaje>> getListaFichajes() { return listaFichajes; }
    public MutableLiveData<String> getResumenSemanal() { return resumenSemanal; }

    public void cargarHistorial(String token) {
        RetrofitClient.getInstance().getMyApi().getHistorial(token).enqueue(new Callback<List<Fichaje>>() {
            @Override
            public void onResponse(Call<List<Fichaje>> call, Response<List<Fichaje>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta = response.body();

                    listaFichajes.setValue(listaCompleta);
                    calcularTotalSemanal(listaCompleta);
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

    private void calcularTotalSemanal(List<Fichaje> datos) {
        long totalMinutos = 0;
        // Aquí sumamos TODO el historial.
        // Para hacerlo "Semanal" real habría que filtrar por fecha con Calendar,
        // pero para empezar sumaremos todo lo que devuelve la API (que suele ser el mes o recientes).
        for (Fichaje f : datos) {
            totalMinutos += f.getMinutosTrabajados();
        }

        long horas = TimeUnit.MINUTES.toHours(totalMinutos);
        long minutos = totalMinutos % 60;

        resumenSemanal.setValue(horas + "h " + minutos + "m");
    }

    // --- NUEVO MÉTODO: FILTRAR ---
    public void filtrarPorRango(Long fechaInicio, Long fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            // Si no hay fechas, restauramos la lista completa
            listaFichajes.setValue(listaCompleta);
            calcularTotalSemanal(listaCompleta);
            return;
        }

        List<Fichaje> listaFiltrada = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Fichaje f : listaCompleta) {
            try {
                // Convertir la fecha del fichaje (String) a Timestamp
                Date dateFichaje = sdf.parse(f.getFecha());
                long timeFichaje = dateFichaje.getTime();

                // Comprobar si está dentro del rango
                if (timeFichaje >= fechaInicio && timeFichaje <= fechaFin) {
                    listaFiltrada.add(f);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // Actualizamos la pantalla con la lista filtrada
        listaFichajes.setValue(listaFiltrada);
        calcularTotalSemanal(listaFiltrada); // Recalcula las horas solo de esos días
    }
}