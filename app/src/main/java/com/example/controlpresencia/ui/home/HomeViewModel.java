package com.example.controlpresencia.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.controlpresencia.data.repository.HomeRepository;

// El ViewModel de la pantalla de inicio. Se encarga de la lógica de negocio y de hablar con el repositorio.
// Mantiene los datos vivos aunque la pantalla se gire o cambie.
public class HomeViewModel extends ViewModel {
    private HomeRepository repository;
    // Mensajes que queremos que la pantalla muestre al usuario.
    private MutableLiveData<String> statusMessage = new MutableLiveData<>();
    // Indica si se está haciendo alguna operación en el servidor para poner el circulito de carga.
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public HomeViewModel() {
        repository = new HomeRepository();
    }

    public LiveData<String> getStatusMessage() { return statusMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // Llama al repositorio para fichar la entrada con las coordenadas GPS.
    public void ficharEntrada(String token, double lat, double lon) {
        isLoading.setValue(true); // Empezamos a cargar.
        repository.registrarEntrada(token, lat, lon, new HomeRepository.FichajeCallback() {
            @Override
            public void onSuccess(String message) {
                isLoading.setValue(false); // Ya hemos terminado.
                statusMessage.setValue(message);
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                statusMessage.setValue(error);
            }
        });
    }

    // Llama al repositorio para fichar la salida.
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