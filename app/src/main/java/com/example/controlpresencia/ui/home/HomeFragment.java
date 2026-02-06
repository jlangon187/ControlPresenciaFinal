package com.example.controlpresencia.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.Empresa;
import com.example.controlpresencia.data.model.User;
import com.example.controlpresencia.data.network.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

// IMPORTS DE OSMDROID
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SessionManager sessionManager;
    private ProgressBar progressBar;
    private Button btnEntrada, btnSalida;

    // Variables del Mapa OSM
    private MapView map;
    private User usuarioActual;

    // Cliente GPS
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 100;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // IMPORTANTE: Configurar OSM antes de inflar la vista
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializar componentes
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        Button btnHistorial = view.findViewById(R.id.btnVerHistorial);

        progressBar = view.findViewById(R.id.progressBarHome);
        btnEntrada = view.findViewById(R.id.btnEntrada);
        btnSalida = view.findViewById(R.id.btnSalida);

        // 2. CONFIGURAR EL MAPA OSM
        map = view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK); // Estilo de mapa estándar
        map.setMultiTouchControls(true); // Permitir zoom con dos dedos
        map.getController().setZoom(18.0); // Zoom inicial cercano

        // 3. Configurar Botones
        btnEntrada.setOnClickListener(v -> {
            if (checkPermission()) {
                obtenerUbicacionYFichar();
            } else {
                pedirPermisoGPS();
            }
        });

        btnHistorial.setOnClickListener(v -> {
            // Navegar al fragmento de Fichajes
            // Asegúrate de que existe esta acción en tu nav_graph.xml
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_fichajesFragment);
        });

        btnSalida.setOnClickListener(v -> {
            String token = sessionManager.getToken();
            if (token != null) viewModel.ficharSalida(token);
        });

        // 4. Observadores
        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), mensaje ->
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show());

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnEntrada.setEnabled(!isLoading);
            btnSalida.setEnabled(!isLoading);
        });

        // 5. Cargar datos del mapa (Empresa y Mi Ubicación)
        cargarPerfilEmpresa();
    }

    // --- LÓGICA DEL MAPA ---

    private void cargarPerfilEmpresa() {
        String token = sessionManager.getToken();
        RetrofitClient.getInstance().getMyApi().getPerfil(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    usuarioActual = response.body();
                    actualizarMapa();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) { }
        });
    }

    private void actualizarMapa() {
        if (map == null || usuarioActual == null || usuarioActual.getEmpresa() == null) return;

        Empresa empresa = usuarioActual.getEmpresa();
        if (empresa.getLatitud() != null && empresa.getLongitud() != null) {

            GeoPoint puntoEmpresa = new GeoPoint(empresa.getLatitud(), empresa.getLongitud());
            int radioMetros = empresa.getRadio() != null ? empresa.getRadio() : 100;

            // A. Centrar mapa en la empresa
            map.getController().setCenter(puntoEmpresa);

            // B. Añadir Marcador de la Empresa
            Marker markerEmpresa = new Marker(map);
            markerEmpresa.setPosition(puntoEmpresa);
            markerEmpresa.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            markerEmpresa.setTitle(empresa.getNombre());
            // Puedes poner un icono personalizado si quieres: markerEmpresa.setIcon(...)
            map.getOverlays().add(markerEmpresa);

            // C. Dibujar Círculo (Radio permitido)
            Polygon circulo = new Polygon();
            // Truco: OSM no tiene "Circle", usamos un polígono de muchos puntos
            List<GeoPoint> puntosCirculo = Polygon.pointsAsCircle(puntoEmpresa, radioMetros);
            circulo.setPoints(puntosCirculo);
            circulo.setFillColor(Color.argb(50, 0, 0, 255)); // Azul transparente
            circulo.setStrokeColor(Color.BLUE);
            circulo.setStrokeWidth(2.0f);
            circulo.setTitle("Zona de Fichaje");
            map.getOverlays().add(circulo);

            // D. Mostrar "Mi Ubicación" (Puntito azul)
            if (checkPermission()) {
                MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
                myLocationOverlay.enableMyLocation();
                map.getOverlays().add(myLocationOverlay);
            }

            map.invalidate(); // Refrescar mapa
        }
    }

    // --- MÉTODOS DE PERMISOS Y FICHAJE (Igual que antes) ---

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void pedirPermisoGPS() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionYFichar();
                actualizarMapa(); // Para activar el puntito azul si ya cargó el perfil
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionYFichar() {
        progressBar.setVisibility(View.VISIBLE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            progressBar.setVisibility(View.GONE);
            if (location != null) {
                String token = sessionManager.getToken();
                if (token != null) {
                    viewModel.ficharEntrada(token, location.getLatitude(), location.getLongitude());
                }
            } else {
                Toast.makeText(getContext(), "Activa el GPS", Toast.LENGTH_SHORT).show();
            }
        });
    }
}