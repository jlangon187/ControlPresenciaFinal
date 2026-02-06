package com.example.controlpresencia.ui.fichajes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair; // Necesario para el rango de fechas
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.google.android.material.datepicker.MaterialDatePicker; // Necesario para el calendario

public class FichajesFragment extends Fragment {

    private FichajesViewModel viewModel;
    private FichajesAdapter adapter;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.fragment_fichajes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(FichajesViewModel.class);

        // 1. Configurar RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvFichajes);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FichajesAdapter();
        rv.setAdapter(adapter);

        TextView tvResumen = view.findViewById(R.id.tvResumenSemanal);

        // 2. Observadores (MVVM)
        viewModel.getListaFichajes().observe(getViewLifecycleOwner(), lista -> {
            adapter.setDatos(lista);
        });

        viewModel.getResumenSemanal().observe(getViewLifecycleOwner(), total -> {
            tvResumen.setText(total);
        });

        // 3. Configurar Botón de Filtro (NUEVO)
        // Al pulsar, abrimos el calendario
        view.findViewById(R.id.btnFiltrar).setOnClickListener(v -> mostrarCalendario());

        // 4. Cargar datos iniciales
        String token = sessionManager.getToken();
        if (token != null) {
            viewModel.cargarHistorial(token);
        }
    }

    // --- MÉTODOS DE CALENDARIO ---

    private void mostrarCalendario() {
        // Creamos el selector de RANGO de fechas (Date Range Picker)
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Selecciona el periodo")
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()))
                .build();

        // Al pulsar "OK" en el calendario
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // selection.first = Fecha Inicio
            // selection.second = Fecha Fin
            viewModel.filtrarPorRango(selection.first, selection.second);
        });

        // (Opcional) Si cancelan o quieren ver todo de nuevo
        datePicker.addOnNegativeButtonClickListener(v -> {
            viewModel.filtrarPorRango(null, null); // Restaurar lista completa
        });

        datePicker.show(getParentFragmentManager(), "FiltroFechas");
    }
}