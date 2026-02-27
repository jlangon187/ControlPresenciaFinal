package com.example.controlpresencia.ui.fichajes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.google.android.material.datepicker.MaterialDatePicker;

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

        RecyclerView rv = view.findViewById(R.id.rvFichajes);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FichajesAdapter();
        rv.setAdapter(adapter);

        TextView tvTrabajado = view.findViewById(R.id.tvResumenTrabajado);
        TextView tvExtra = view.findViewById(R.id.tvResumenExtra);

        viewModel.getListaFichajes().observe(getViewLifecycleOwner(), lista -> {
            adapter.setDatos(lista);
        });

        viewModel.getResumenTrabajado().observe(getViewLifecycleOwner(), tvTrabajado::setText);
        viewModel.getResumenExtra().observe(getViewLifecycleOwner(), tvExtra::setText);

        view.findViewById(R.id.btnFiltrar).setOnClickListener(v -> mostrarCalendario());

        // Lógica para el botón volver
        View btnVolver = view.findViewById(R.id.btnVolverFichajes);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigateUp());
        }

        String token = sessionManager.getToken();
        if (token != null) {
            viewModel.cargarHistorial(token);
        }
    }

    private void mostrarCalendario() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Selecciona el periodo")
                .setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()))
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            viewModel.filtrarPorRango(selection.first, selection.second);
        });

        datePicker.addOnNegativeButtonClickListener(v -> {
            viewModel.filtrarPorRango(null, null);
        });

        datePicker.show(getParentFragmentManager(), "FiltroFechas");
    }
}