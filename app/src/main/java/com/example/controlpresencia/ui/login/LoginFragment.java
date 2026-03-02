package com.example.controlpresencia.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;

// Esta es la pantalla de inicio de sesión.
public class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgotPassword;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle saved) {
        // Inflamos el diseño de la pantalla de login.
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Si ya hay un token guardado, pasamos directamente a la pantalla principal sin pedir login.
        SessionManager sessionManager = new SessionManager(requireContext());
        if (sessionManager.getToken() != null) {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_loginFragment_to_homeFragment);
            return;
        }

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);


        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Al pulsar el botón de entrar.
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);

                // Antes de loguearnos, pedimos el token de Firebase para que el servidor nos pueda mandar notificaciones.
                com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            String fcmToken = null;
                            if (task.isSuccessful() && task.getResult() != null) {
                                fcmToken = task.getResult();
                                android.util.Log.d("LOGIN_FCM", "Token obtenido: " + fcmToken);
                            } else {
                                android.util.Log.e("LOGIN_FCM", "Error al obtener token Firebase", task.getException());
                            }

                            // Llamamos al ViewModel para que gestione el inicio de sesión.
                            viewModel.login(email, password, fcmToken);
                        });
            } else {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        // Al pulsar en "Olvidé mi contraseña".
        tvForgotPassword.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
        });

        // Si el servidor devuelve un error de login.
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        });

        // Mostramos o quitamos la barra de carga según el estado.
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading);
        });

        // Si el login ha ido bien, guardamos la sesión y entramos.
        viewModel.getLoginResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                SessionManager session = new SessionManager(requireContext());
                session.saveSession(response.getAccessToken(), response.getRol(), response.getNombre());

                Toast.makeText(getContext(), "¡Bienvenido " + response.getNombre() + "!", Toast.LENGTH_SHORT).show();

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_loginFragment_to_homeFragment);
            }
        });
    }
}