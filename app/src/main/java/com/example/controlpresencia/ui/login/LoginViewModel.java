package com.example.controlpresencia.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.controlpresencia.data.model.LoginResponse;
import com.example.controlpresencia.data.repository.LoginRepository;

// Este es el ViewModel para el login. Se encarga de llamar al repositorio para validar al usuario.
public class LoginViewModel extends ViewModel {

    private LoginRepository repository;
    // Aquí guardamos la respuesta del servidor si el login va bien.
    private MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    // Aquí guardamos el mensaje de error si algo falla.
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    // Sirve para saber si estamos esperando respuesta del servidor.
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        repository = new LoginRepository();
    }

    public LiveData<LoginResponse> getLoginResponse() { return loginResponse; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // Método que lanza la petición de login al repositorio.
    public void login(String email, String password, String fcmToken) {
        isLoading.setValue(true);

        repository.hacerLogin(email, password, fcmToken, new LoginRepository.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                isLoading.setValue(false);
                loginResponse.setValue(response);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}