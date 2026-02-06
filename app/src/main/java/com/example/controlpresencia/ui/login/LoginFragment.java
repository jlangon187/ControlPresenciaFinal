package com.example.controlpresencia.ui.login;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializar vistas
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);

        // 2. Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 3. Configurar el botón
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                viewModel.login(email, password);
            } else {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Observar cambios (MVVM)

        // Si hay error...
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        });

        // Si está cargando...
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!isLoading); // Desactivar botón mientras carga
        });

        viewModel.getLoginToken().observe(getViewLifecycleOwner(), token -> {
            if (token != null) {
                // 1. Guardar el token en preferencias
                SessionManager session = new SessionManager(requireContext());
                session.saveToken(token);

                Toast.makeText(getContext(), "Login Correcto", Toast.LENGTH_SHORT).show();

                // 2. Navegar al Home
                NavController navController = Navigation.findNavController(view);
                // IMPORTANTE: Usamos popUpTo para borrar el login del historial (botón atrás no vuelve al login)
                navController.navigate(R.id.action_loginFragment_to_homeFragment);
            }
        });
    }
}