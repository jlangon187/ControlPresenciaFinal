package com.example.controlpresencia.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.model.ResetPasswordRequest;
import com.example.controlpresencia.data.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordFragment extends Fragment {

    private EditText etEmail;
    private Button btnEnviar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        etEmail = view.findViewById(R.id.etEmailReset);
        btnEnviar = view.findViewById(R.id.btnEnviarReset);
        Button btnVolver = view.findViewById(R.id.btnVolverLogin);

        btnEnviar.setOnClickListener(v -> enviarSolicitud());
        btnVolver.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void enviarSolicitud() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Escribe tu email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviar.setEnabled(false);
        btnEnviar.setText("Enviando...");

        ResetPasswordRequest request = new ResetPasswordRequest(email);

        // Nota: Esta llamada NO necesita token porque el usuario aún no está logueado
        RetrofitClient.getInstance().getMyApi().solicitarResetPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("ENVIAR CORREO");

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "✅ Revisa tu correo", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(getView()).navigateUp(); // Volver al login
                } else {
                    Toast.makeText(getContext(), "Error al solicitar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnEnviar.setEnabled(true);
                btnEnviar.setText("ENVIAR CORREO");
                Toast.makeText(getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}