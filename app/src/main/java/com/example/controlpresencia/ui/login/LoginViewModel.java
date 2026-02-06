package com.example.controlpresencia.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.controlpresencia.data.repository.LoginRepository;

public class LoginViewModel extends ViewModel {

    private LoginRepository repository;

    // LiveData: Son observables que la Vista (Fragment) mirará
    private MutableLiveData<String> loginToken = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        repository = new LoginRepository();
    }

    // Getters para observar desde el Fragment
    public LiveData<String> getLoginToken() { return loginToken; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // Método que llama el Fragment al pulsar el botón
    public void login(String email, String password) {
        isLoading.setValue(true); // Mostrar carga

        repository.hacerLogin(email, password, new LoginRepository.LoginCallback() {
            @Override
            public void onSuccess(String token) {
                isLoading.setValue(false);
                loginToken.setValue(token); // Notificar éxito
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message); // Notificar error
            }
        });
    }
}