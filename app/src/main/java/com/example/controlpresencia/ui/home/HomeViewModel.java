package com.example.controlpresencia.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.controlpresencia.data.repository.HomeRepository;

public class HomeViewModel extends ViewModel {
    private HomeRepository repository;
    private MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public HomeViewModel() {
        repository = new HomeRepository();
    }

    public LiveData<String> getStatusMessage() { return statusMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void ficharEntrada(String token, double lat, double lon) {
        isLoading.setValue(true);
        repository.registrarEntrada(token, lat, lon, new HomeRepository.FichajeCallback() {
            @Override
            public void onSuccess(String message) {
                isLoading.setValue(false);
                statusMessage.setValue(message);
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                statusMessage.setValue(error);
            }
        });
    }

    public void ficharSalida(String token) {
        isLoading.setValue(true);
        repository.registrarSalida(token, new HomeRepository.FichajeCallback() {
            @Override
            public void onSuccess(String message) {
                isLoading.setValue(false);
                statusMessage.setValue(message);
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                statusMessage.setValue(error);
            }
        });
    }
}