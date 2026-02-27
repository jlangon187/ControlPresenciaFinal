package com.example.controlpresencia.ui.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.ConfigUbicacionRequest;
import com.example.controlpresencia.data.model.Empresa;
import com.example.controlpresencia.data.model.FichajeResponse;
import com.example.controlpresencia.data.model.User;
import com.example.controlpresencia.data.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigMapaFragment extends Fragment {

    private MapView mapAdmin;
    private Slider sliderRadio;
    private TextView tvMetrosActuales;
    private SessionManager sessionManager;
    private Polygon circuloRadioNaranja;
    private Polygon circuloActualRojo;
    private GpsMyLocationProvider gpsProvider;

    private Integer empresaIdSeleccionada = null;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    centrarMapaEnMiUbicacion();
                } else {
                    Toast.makeText(getContext(), "Permiso de GPS denegado. Mueve el mapa manualmente.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().load(requireContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()));
        return inflater.inflate(R.layout.fragment_config_mapa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        if (getArguments() != null && getArguments().containsKey("empresa_id")) {
            empresaIdSeleccionada = getArguments().getInt("empresa_id");
        }

        mapAdmin = view.findViewById(R.id.mapAdmin);
        sliderRadio = view.findViewById(R.id.sliderRadio);
        tvMetrosActuales = view.findViewById(R.id.tvMetrosActuales);
        MaterialButton btnVolver = view.findViewById(R.id.btnVolverMapa);
        MaterialButton btnGuardar = view.findViewById(R.id.btnGuardarUbicacion);

        mapAdmin.setMultiTouchControls(true);
        mapAdmin.getController().setZoom(18.0);
        mapAdmin.getController().setCenter(new GeoPoint(40.416775, -3.703790)); // Por defecto

        circuloRadioNaranja = new Polygon();
        circuloRadioNaranja.setFillColor(Color.parseColor("#33EA580C")); // Naranja transparente
        circuloRadioNaranja.setStrokeColor(Color.parseColor("#EA580C"));
        circuloRadioNaranja.setStrokeWidth(3.0f);
        mapAdmin.getOverlays().add(circuloRadioNaranja);

        actualizarCirculoNaranjaEnMapa();

        mapAdmin.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) { actualizarCirculoNaranjaEnMapa(); return false; }
            @Override
            public boolean onZoom(ZoomEvent event) { actualizarCirculoNaranjaEnMapa(); return false; }
        });

        sliderRadio.addOnChangeListener((slider, value, fromUser) -> {
            tvMetrosActuales.setText((int) value + "m");
            actualizarCirculoNaranjaEnMapa();
        });

        btnVolver.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnGuardar.setOnClickListener(v -> guardarNuevaUbicacion());

        cargarUbicacionActualDeEmpresa();
    }

    // --- LÓGICA DE CARGA DE DATOS ---
    private void cargarUbicacionActualDeEmpresa() {
        String token = sessionManager.getToken();

        if (empresaIdSeleccionada != null) {
            // 1. ES SUPERADMIN: Buscamos las coordenadas en la lista de empresas
            RetrofitClient.getInstance().getMyApi().getEmpresasAdmin(token).enqueue(new Callback<List<Empresa>>() {
                @Override
                public void onResponse(Call<List<Empresa>> call, Response<List<Empresa>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (Empresa emp : response.body()) {
                            if (emp.getIdEmpresa() == empresaIdSeleccionada) {
                                dibujarZonaRoja(emp);
                                return;
                            }
                        }
                    }
                    buscarMiGPS(); // Si no la encuentra, al GPS
                }
                @Override
                public void onFailure(Call<List<Empresa>> call, Throwable t) { buscarMiGPS(); }
            });
        } else {
            // 2. ES ADMIN NORMAL: Sacamos las coordenadas de su propio perfil
            RetrofitClient.getInstance().getMyApi().getPerfil(token).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Empresa empresa = response.body().getEmpresa();
                        if (empresa != null && empresa.getLatitud() != null) {
                            dibujarZonaRoja(empresa);
                            return;
                        }
                    }
                    buscarMiGPS(); // Si no tiene coordenadas aún, al GPS
                }
                @Override
                public void onFailure(Call<User> call, Throwable t) { buscarMiGPS(); }
            });
        }
    }

    private void dibujarZonaRoja(Empresa empresa) {
        if (empresa.getLatitud() == null || empresa.getLongitud() == null) {
            buscarMiGPS();
            return;
        }

        GeoPoint puntoAntiguo = new GeoPoint(empresa.getLatitud(), empresa.getLongitud());
        int radioAntiguo = empresa.getRadio() != null ? empresa.getRadio() : 100;

        // Poner un marcador en el punto central original
        Marker marker = new Marker(mapAdmin);
        marker.setPosition(puntoAntiguo);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Ubicación Actual Guardada");
        mapAdmin.getOverlays().add(marker);

        // Dibujar círculo rojo usando los métodos que funcionan bien en osmdroid
        circuloActualRojo = new Polygon();
        circuloActualRojo.setPoints(Polygon.pointsAsCircle(puntoAntiguo, radioAntiguo));
        circuloActualRojo.setFillColor(Color.argb(20, 255, 0, 0)); // Rojo muy sutil para que no tape
        circuloActualRojo.setStrokeColor(Color.RED);
        circuloActualRojo.setStrokeWidth(4.0f);
        mapAdmin.getOverlays().add(circuloActualRojo);

        // Asegurarnos de que el naranja siempre esté por encima del rojo
        mapAdmin.getOverlays().remove(circuloRadioNaranja);
        mapAdmin.getOverlays().add(circuloRadioNaranja);

        // Centrar el mapa directamente ahí
        mapAdmin.getController().setCenter(puntoAntiguo);

        // Ajustamos el slider al radio que tenían guardado
        sliderRadio.setValue((float) radioAntiguo);

        mapAdmin.invalidate(); // Refrescar mapa
    }

    private void buscarMiGPS() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            centrarMapaEnMiUbicacion();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void centrarMapaEnMiUbicacion() {
        Toast.makeText(getContext(), "Buscando tu ubicación...", Toast.LENGTH_SHORT).show();
        gpsProvider = new GpsMyLocationProvider(requireContext());
        gpsProvider.startLocationProvider(new IMyLocationConsumer() {
            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {
                if (location != null && getActivity() != null) {
                    GeoPoint miPuntoActual = new GeoPoint(location.getLatitude(), location.getLongitude());

                    getActivity().runOnUiThread(() -> {
                        mapAdmin.getController().animateTo(miPuntoActual);
                        actualizarCirculoNaranjaEnMapa();
                    });

                    gpsProvider.stopLocationProvider();
                }
            }
        });
    }

    private void actualizarCirculoNaranjaEnMapa() {
        if (mapAdmin == null || circuloRadioNaranja == null) return;
        GeoPoint centroActual = (GeoPoint) mapAdmin.getMapCenter();
        double radioEnMetros = sliderRadio.getValue();
        circuloRadioNaranja.setPoints(Polygon.pointsAsCircle(centroActual, radioEnMetros));
        mapAdmin.invalidate();
    }

    private void guardarNuevaUbicacion() {
        GeoPoint centro = (GeoPoint) mapAdmin.getMapCenter();
        double radio = sliderRadio.getValue();
        String token = sessionManager.getToken();

        Toast.makeText(getContext(), "Guardando en servidor...", Toast.LENGTH_SHORT).show();

        ConfigUbicacionRequest request = new ConfigUbicacionRequest(centro.getLatitude(), centro.getLongitude(), radio, empresaIdSeleccionada);

        RetrofitClient.getInstance().getMyApi().configurarUbicacionEmpresa(token, request).enqueue(new Callback<FichajeResponse>() {
            @Override
            public void onResponse(Call<FichajeResponse> call, Response<FichajeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "✅ " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                    if (getView() != null) Navigation.findNavController(getView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), "❌ Error al guardar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FichajeResponse> call, Throwable t) {
                Toast.makeText(getContext(), "⚠️ Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (gpsProvider != null) {
            gpsProvider.stopLocationProvider();
        }
    }
}