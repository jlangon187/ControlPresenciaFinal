package com.example.controlpresencia.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.controlpresencia.data.model.LoginResponse;
import com.example.controlpresencia.data.repository.LoginRepository;

public class LoginViewModel extends ViewModel {

    private LoginRepository repository;
    private MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        repository = new LoginRepository();
    }

    public LiveData<LoginResponse> getLoginResponse() { return loginResponse; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

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