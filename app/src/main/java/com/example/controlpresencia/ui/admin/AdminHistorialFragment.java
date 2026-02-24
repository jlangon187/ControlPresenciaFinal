package com.example.controlpresencia.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.Fichaje;
import com.example.controlpresencia.data.network.RetrofitClient;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHistorialFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvTitulo, tvTrabajado, tvExtra;
    private SessionManager sessionManager;
    private int idTrabajador;
    private String nombreTrabajador;

    private List<Fichaje> listaCompleta = new ArrayList<>();
    private AdminHistorialAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.fragment_admin_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sessionManager = new SessionManager(requireContext());
        recyclerView = view.findViewById(R.id.rvAdminHistorial);
        tvTitulo = view.findViewById(R.id.tvTituloHistorialAdmin);
        tvTrabajado = view.findViewById(R.id.tvAdminResumenTrabajado);
        tvExtra = view.findViewById(R.id.tvAdminResumenExtra);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            idTrabajador = getArguments().getInt("id_trabajador");
            nombreTrabajador = getArguments().getString("nombre_trabajador");
            tvTitulo.setText("Historial de " + nombreTrabajador);
        }

        view.findViewById(R.id.btnAdminFiltrar).setOnClickListener(v -> mostrarCalendario());

        cargarHistorial();
    }

    private void cargarHistorial() {
        String token = sessionManager.getToken();
        RetrofitClient.getInstance().getMyApi().getHistorialAdmin(token, idTrabajador).enqueue(new Callback<List<Fichaje>>() {
            @Override
            public void onResponse(Call<List<Fichaje>> call, Response<List<Fichaje>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCompleta = response.body();
                    actualizarListaYTotales(listaCompleta);
                } else {
                    Toast.makeText(getContext(), "No hay registros o error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Fichaje>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarListaYTotales(List<Fichaje> datosMostrar) {
        // 1. Mostrar lista
        adapter = new AdminHistorialAdapter(datosMostrar, fichaje -> {
            if (fichaje.getLatitud() != null && fichaje.getLongitud() != null) {
                Bundle bundle = new Bundle();
                bundle.putDouble("lat", fichaje.getLatitud());
                bundle.putDouble("lng", fichaje.getLongitud());
                bundle.putString("nombre", nombreTrabajador);
                bundle.putString("fecha", fichaje.getFecha());
                bundle.putString("hora_entrada", fichaje.getHoraEntradaFormateada());
                bundle.putString("hora_salida", fichaje.getHoraSalidaFormateada());
                Navigation.findNavController(getView()).navigate(R.id.action_adminHistorial_to_mapDetail, bundle);
            } else {
                Toast.makeText(getContext(), "Sin coordenadas GPS", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        // 2. Calcular totales
        long totalMinutos = 0;
        double totalExtrasDec = 0.0;

        for (Fichaje f : datosMostrar) {
            totalMinutos += f.getMinutosTrabajados();
            totalExtrasDec += f.getHorasExtra();
        }

        long horas = TimeUnit.MINUTES.toHours(totalMinutos);
        long minutos = totalMinutos % 60;
        tvTrabajado.setText(horas + "h " + minutos + "m");

        long extraMinutosTotales = Math.round(totalExtrasDec * 60);
        long hExtra = extraMinutosTotales / 60;
        long mExtra = extraMinutosTotales % 60;

        if (extraMinutosTotales > 0) {
            tvExtra.setText("+" + hExtra + "h " + String.format(Locale.getDefault(), "%02d", mExtra) + "m");
        } else {
            tvExtra.setText("0h 00m");
        }
    }

    private void mostrarCalendario() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Selecciona el periodo")
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()))
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            filtrarPorRango(selection.first, selection.second);
        });

        datePicker.addOnNegativeButtonClickListener(v -> {
            actualizarListaYTotales(listaCompleta); // Restaurar todo
        });

        datePicker.show(getParentFragmentManager(), "FiltroFechasAdmin");
    }

    private void filtrarPorRango(Long fechaInicio, Long fechaFin) {
        if (fechaInicio == null || fechaFin == null) return;

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

        actualizarListaYTotales(listaFiltrada);
    }
}