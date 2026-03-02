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

// Esta pantalla es para que el administrador configure dónde está la oficina y el radio permitido para fichar.
public class ConfigMapaFragment extends Fragment {

    private MapView mapAdmin;
    private Slider sliderRadio;
    private TextView tvMetrosActuales;
    private SessionManager sessionManager;
    private Polygon circuloRadioNaranja; // El círculo que se mueve con el mapa (nueva ubicación).
    private Polygon circuloActualRojo;   // El círculo que indica la ubicación que ya está guardada.
    private GpsMyLocationProvider gpsProvider;

    private Integer empresaIdSeleccionada = null;

    // Lanzador para pedir permisos de ubicación si no los tenemos.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    centrarMapaEnMiUbicacion();
                } else {
                    Toast.makeText(getContext(), "Permiso de GPS denegado. Mueve el mapa manualmente.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        // Carga la configuración del mapa.
        Configuration.getInstance().load(requireContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()));
        return inflater.inflate(R.layout.fragment_config_mapa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        // Miramos si nos han pasado una empresa específica para configurar.
        if (getArguments() != null && getArguments().containsKey("empresa_id")) {
            empresaIdSeleccionada = getArguments().getInt("empresa_id");
        }

        mapAdmin = view.findViewById(R.id.mapAdmin);
        sliderRadio = view.findViewById(R.id.sliderRadio);
        tvMetrosActuales = view.findViewById(R.id.tvMetrosActuales);
        MaterialButton btnVolver = view.findViewById(R.id.btnVolverMapa);
        MaterialButton btnGuardar = view.findViewById(R.id.btnGuardarUbicacion);

        // Configuración inicial del mapa.
        mapAdmin.setMultiTouchControls(true);
        mapAdmin.getController().setZoom(18.0);
        mapAdmin.getController().setCenter(new GeoPoint(40.416775, -3.703790)); // Centramos en Madrid por si acaso.

        // Creamos el círculo naranja que servirá para elegir la nueva zona.
        circuloRadioNaranja = new Polygon();
        circuloRadioNaranja.setFillColor(Color.parseColor("#33EA580C")); // Naranja clarito.
        circuloRadioNaranja.setStrokeColor(Color.parseColor("#EA580C"));
        circuloRadioNaranja.setStrokeWidth(3.0f);
        mapAdmin.getOverlays().add(circuloRadioNaranja);

        actualizarCirculoNaranjaEnMapa();

        // Escuchamos cuando el usuario mueve el mapa para centrar el círculo naranja.
        mapAdmin.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) { actualizarCirculoNaranjaEnMapa(); return false; }
            @Override
            public boolean onZoom(ZoomEvent event) { actualizarCirculoNaranjaEnMapa(); return false; }
        });

        // Actualizamos el tamaño del círculo cuando se mueve el slider del radio.
        sliderRadio.addOnChangeListener((slider, value, fromUser) -> {
            tvMetrosActuales.setText((int) value + "m");
            actualizarCirculoNaranjaEnMapa();
        });

        btnVolver.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnGuardar.setOnClickListener(v -> guardarNuevaUbicacion());

        // Al entrar, intentamos cargar la ubicación que ya tiene la empresa en la base de datos.
        cargarUbicacionActualDeEmpresa();
    }

    // Trae de la API la ubicación actual para enseñarla en el mapa.
    private void cargarUbicacionActualDeEmpresa() {
        String token = sessionManager.getToken();

        if (empresaIdSeleccionada != null) {
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
                    buscarMiGPS();
                }
                @Override
                public void onFailure(Call<List<Empresa>> call, Throwable t) { buscarMiGPS(); }
            });
        } else {
            // Si no hay ID de empresa, tiramos del perfil del usuario logueado.
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
                    buscarMiGPS();
                }
                @Override
                public void onFailure(Call<User> call, Throwable t) { buscarMiGPS(); }
            });
        }
    }

    // Pinta un círculo rojo donde está la oficina actualmente según la base de datos.
    private void dibujarZonaRoja(Empresa empresa) {
        if (empresa.getLatitud() == null || empresa.getLongitud() == null) {
            buscarMiGPS();
            return;
        }

        GeoPoint puntoAntiguo = new GeoPoint(empresa.getLatitud(), empresa.getLongitud());
        int radioAntiguo = empresa.getRadio() != null ? empresa.getRadio() : 100;

        Marker marker = new Marker(mapAdmin);
        marker.setPosition(puntoAntiguo);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Ubicación Actual Guardada");
        mapAdmin.getOverlays().add(marker);

        circuloActualRojo = new Polygon();
        circuloActualRojo.setPoints(Polygon.pointsAsCircle(puntoAntiguo, radioAntiguo));
        circuloActualRojo.setFillColor(Color.argb(20, 255, 0, 0));
        circuloActualRojo.setStrokeColor(Color.RED);
        circuloActualRojo.setStrokeWidth(4.0f);
        mapAdmin.getOverlays().add(circuloActualRojo);

        // Nos aseguramos de que el círculo naranja (el nuevo) quede por encima.
        mapAdmin.getOverlays().remove(circuloRadioNaranja);
        mapAdmin.getOverlays().add(circuloRadioNaranja);

        mapAdmin.getController().setCenter(puntoAntiguo);
        sliderRadio.setValue((float) radioAntiguo);

        mapAdmin.invalidate();
    }

    // Si no hay ubicación guardada, intentamos usar el GPS del móvil para centrar el mapa.
    private void buscarMiGPS() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            centrarMapaEnMiUbicacion();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // Usa el proveedor de GPS para mover el mapa a la posición del administrador.
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

    // Dibuja el círculo naranja siempre en el centro de la pantalla del mapa.
    private void actualizarCirculoNaranjaEnMapa() {
        if (mapAdmin == null || circuloRadioNaranja == null) return;
        GeoPoint centroActual = (GeoPoint) mapAdmin.getMapCenter();
        double radioEnMetros = sliderRadio.getValue();
        circuloRadioNaranja.setPoints(Polygon.pointsAsCircle(centroActual, radioEnMetros));
        mapAdmin.invalidate();
    }

    // Mandamos al servidor las nuevas coordenadas y el radio que ha elegido el administrador.
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