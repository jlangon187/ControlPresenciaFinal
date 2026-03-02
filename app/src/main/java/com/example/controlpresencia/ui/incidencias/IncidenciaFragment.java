package com.example.controlpresencia.ui.incidencias;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.IncidenciaRequest;
import com.example.controlpresencia.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Pantalla para que un trabajador envíe una incidencia (queja, aviso de retraso, etc.) a la empresa.
public class IncidenciaFragment extends Fragment {

    private EditText etTitulo, etDescripcion;
    private Button btnEnviar;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        // Inflamos el diseño de la pantalla de nueva incidencia.
        return inflater.inflate(R.layout.fragment_incidencia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sessionManager = new SessionManager(requireContext());

        etTitulo = view.findViewById(R.id.etTituloIncidencia);
        etDescripcion = view.findViewById(R.id.etDescripcionIncidencia);
        btnEnviar = view.findViewById(R.id.btnEnviarIncidencia);
        progressBar = view.findViewById(R.id.progressBarIncidencia);

        // Al pulsar enviar, se manda la información al servidor.
        btnEnviar.setOnClickListener(v -> enviarIncidencia());

        // Botón para volver atrás.
        View btnVolver = view.findViewById(R.id.btnVolverIncidencia);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }
    }

    // Recoge los datos de los campos de texto y los manda a la API.
    private void enviarIncidencia() {
        String titulo = etTitulo.getText().toString().trim();
        String desc = etDescripcion.getText().toString().trim();

        // Validamos que el usuario haya escrito algo.
        if (titulo.isEmpty() || desc.isEmpty()) {
            Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnEnviar.setEnabled(false);

        String token = sessionManager.getToken();
        IncidenciaRequest request = new IncidenciaRequest(titulo, desc);

        RetrofitClient.getInstance().getMyApi().crearIncidencia(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (getView() == null) return;

                progressBar.setVisibility(View.GONE);
                btnEnviar.setEnabled(true);

                if (response.isSuccessful()) {
                    // Si todo ha ido bien, avisamos y volvemos a la pantalla anterior.
                    Toast.makeText(getContext(), "✅ Incidencia enviada correctamente", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(getView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Error al enviar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (getView() == null) return;

                progressBar.setVisibility(View.GONE);
                btnEnviar.setEnabled(true);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}