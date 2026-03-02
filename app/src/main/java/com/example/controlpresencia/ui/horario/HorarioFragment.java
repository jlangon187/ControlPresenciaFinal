package com.example.controlpresencia.ui.horario;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.DiaHorario;
import com.example.controlpresencia.data.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Pantalla para que el trabajador vea su horario semanal asignado.
public class HorarioFragment extends Fragment {

    private RecyclerView rvDiasHorario;
    private ProgressBar progressBarHorario;
    private HorarioAdapter adapter;
    private SessionManager sessionManager;
    private TextView tvTipoJornada, tvHorasSemanales;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        // Inflamos el diseño de la pantalla de horario.
        return inflater.inflate(R.layout.fragment_horario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sessionManager = new SessionManager(requireContext());

        rvDiasHorario = view.findViewById(R.id.rvDiasHorario);
        progressBarHorario = view.findViewById(R.id.progressBarHorario);
        tvTipoJornada = view.findViewById(R.id.tvTipoJornada);
        tvHorasSemanales = view.findViewById(R.id.tvHorasSemanales);

        // Botón para volver atrás.
        MaterialButton btnVolver = view.findViewById(R.id.btnVolverHorario);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        // Configuramos la lista verticalmente.
        rvDiasHorario.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HorarioAdapter(new ArrayList<>());
        rvDiasHorario.setAdapter(adapter);

        // Pedimos el horario al servidor.
        cargarHorarioDesdeAPI();
    }

    // Llama a la API para obtener los días y horas de trabajo del usuario logueado.
    private void cargarHorarioDesdeAPI() {
        progressBarHorario.setVisibility(View.VISIBLE);
        String token = sessionManager.getToken();

        RetrofitClient.getInstance().getMyApi().getMiHorario(token).enqueue(new Callback<List<DiaHorario>>() {
            @Override
            public void onResponse(Call<List<DiaHorario>> call, Response<List<DiaHorario>> response) {
                if (getView() == null) return;
                progressBarHorario.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<DiaHorario> dias = response.body();

                    // Si no tiene horario, avisamos y limpiamos los textos.
                    if (dias.isEmpty()) {
                        adapter.setDias(new ArrayList<>());
                        tvHorasSemanales.setText("--h");
                        tvTipoJornada.setText("Sin asignar");
                        Toast.makeText(getContext(), "Aún no tienes un horario asignado.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Actualizamos la lista con los días recibidos.
                    adapter.setDias(dias);

                    // Calculamos una estimación de horas semanales (suponiendo 8h por día no libre).
                    int horasTotales = 0;
                    for (DiaHorario dia : dias) {
                        if (!dia.isEsLibre()) {
                            horasTotales += 8;
                        }
                    }

                    tvHorasSemanales.setText(horasTotales + "h");
                    tvTipoJornada.setText(horasTotales >= 35 ? "Completa" : "Parcial");

                } else {
                    Toast.makeText(getContext(), "Error al cargar el horario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DiaHorario>> call, Throwable t) {
                if (getView() == null) return;
                progressBarHorario.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}