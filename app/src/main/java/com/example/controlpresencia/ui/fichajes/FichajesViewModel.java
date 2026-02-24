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
    private MutableLiveData<String> resumenTrabajado = new MutableLiveData<>();
    private MutableLiveData<String> resumenExtra = new MutableLiveData<>();
    private MutableLiveData<String> errorMsg = new MutableLiveData<>();

    public MutableLiveData<List<Fichaje>> getListaFichajes() { return listaFichajes; }
    public MutableLiveData<String> getResumenTrabajado() { return resumenTrabajado; }
    public MutableLiveData<String> getResumenExtra() { return resumenExtra; }

    public void cargarHistorial(String token) {
        RetrofitClient.getInstance().getMyApi().getHistorial(token).enqueue(new Callback<List<Fichaje>>() {
            @Override
            public void onResponse(Call<List<Fichaje>> call, Response<List<Fichaje>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta = response.body();
                    listaFichajes.setValue(listaCompleta);
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

    private void calcularTotales(List<Fichaje> datos) {
        long totalMinutos = 0;
        double totalExtrasDec = 0.0;

        for (Fichaje f : datos) {
            totalMinutos += f.getMinutosTrabajados();
            totalExtrasDec += f.getHorasExtra();
        }

        long horas = TimeUnit.MINUTES.toHours(totalMinutos);
        long minutos = totalMinutos % 60;
        resumenTrabajado.setValue(horas + "h " + minutos + "m");

        long extraMinutosTotales = Math.round(totalExtrasDec * 60);
        long hExtra = extraMinutosTotales / 60;
        long mExtra = extraMinutosTotales % 60;

        if (extraMinutosTotales > 0) {
            resumenExtra.setValue("+" + hExtra + "h " + String.format(Locale.getDefault(), "%02d", mExtra) + "m");
        } else {
            resumenExtra.setValue("0h 00m");
        }
    }

    public void filtrarPorRango(Long fechaInicio, Long fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            listaFichajes.setValue(listaCompleta);
            calcularTotales(listaCompleta);
            return;
        }

        List<Fichaje> listaFiltrada = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Fichaje f : listaCompleta) {
            try {
                Date dateFichaje = sdf.parse(f.getFecha());
                long timeFichaje = dateFichaje.getTime();
                if (timeFichaje >= fechaInicio && timeFichaje <= fechaFin) {
                    listaFiltrada.add(f);
                }
            } catch (Exception ignored) {}
        }

        listaFichajes.setValue(listaFiltrada);
        calcularTotales(listaFiltrada);
    }
}