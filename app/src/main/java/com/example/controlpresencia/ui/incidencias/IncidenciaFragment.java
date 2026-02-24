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

public class IncidenciaFragment extends Fragment {

    private EditText etTitulo, etDescripcion;
    private Button btnEnviar;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.fragment_incidencia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sessionManager = new SessionManager(requireContext());

        etTitulo = view.findViewById(R.id.etTituloIncidencia);
        etDescripcion = view.findViewById(R.id.etDescripcionIncidencia);
        btnEnviar = view.findViewById(R.id.btnEnviarIncidencia);
        btnEnviar.setOnClickListener(v -> enviarIncidencia());
    }

    private void enviarIncidencia() {
        String titulo = etTitulo.getText().toString().trim();
        String desc = etDescripcion.getText().toString().trim();

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
                progressBar.setVisibility(View.GONE);
                btnEnviar.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "✅ Incidencia enviada correctamente", Toast.LENGTH_LONG).show();
                    // Volver atrás (al Home)
                    Navigation.findNavController(getView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Error al enviar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnEnviar.setEnabled(true);
                Toast.makeText(getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}