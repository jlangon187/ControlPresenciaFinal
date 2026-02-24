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

public class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgotPassword;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());
        if (sessionManager.getToken() != null) {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_loginFragment_to_homeFragment);
            return; // Detenemos la carga de esta pantalla
        }

        // 1. Inicializar vistas
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);


        // 2. Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 3. Configurar el botón
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                // 1. Mostrar que estamos cargando
                progressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);

                // 2. Pedir el Token a Firebase
                com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            String fcmToken = null;
                            if (task.isSuccessful() && task.getResult() != null) {
                                fcmToken = task.getResult();
                                android.util.Log.d("LOGIN_FCM", "Token obtenido: " + fcmToken);
                            } else {
                                android.util.Log.e("LOGIN_FCM", "Error al obtener token Firebase", task.getException());
                            }

                            // 3. Hacer el Login con el token
                            viewModel.login(email, password, fcmToken);
                        });
            } else {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        tvForgotPassword.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
        });

// 4. Observar cambios (MVVM)
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading);
        });

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