package com.example.controlpresencia.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.button.MaterialButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SessionManager sessionManager;
    private ProgressBar progressBar;
    private Button btnEntrada, btnSalida, btnReportarIncidencia;
    private MapView map;
    private User usuarioActual;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private android.nfc.NfcAdapter nfcAdapter;
    private String uidSimulado = "A1B2C3D4";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Button btnHistorial = view.findViewById(R.id.btnVerHistorial);
        progressBar = view.findViewById(R.id.progressBarHome);
        btnEntrada = view.findViewById(R.id.btnEntrada);
        btnSalida = view.findViewById(R.id.btnSalida);
        btnReportarIncidencia = view.findViewById(R.id.btnReportarIncidencia);
        map = view.findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(18.0);

        nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(requireContext());

        btnEntrada.setOnClickListener(v -> {
            if (checkPermission()) {
                obtenerUbicacionYFichar();
            } else {
                pedirPermisoGPS();
            }
        });

        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();

            androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build();

            Navigation.findNavController(v).navigate(R.id.loginFragment, null, navOptions);
        });

        btnHistorial.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_fichajesFragment));

        btnSalida.setOnClickListener(v -> {
            String token = sessionManager.getToken();
            if (token != null) viewModel.ficharSalida(token);
        });

        btnReportarIncidencia.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_incidenciaFragment));

        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        String nombre = sessionManager.getNombre();
        if (nombre != null && !nombre.isEmpty()) {
            tvWelcome.setText("Hola, " + nombre);
        }

        View dividerAdmin = view.findViewById(R.id.dividerAdmin);
        TextView tvAdminTitle = view.findViewById(R.id.tvAdminTitle);
        MaterialButton btnAdmin = view.findViewById(R.id.btnAdminPanel);

        if (sessionManager.isAdmin()) {
            dividerAdmin.setVisibility(View.VISIBLE);
            tvAdminTitle.setVisibility(View.VISIBLE);
            btnAdmin.setVisibility(View.VISIBLE);

            if (sessionManager.getRol().equalsIgnoreCase("Superadministrador")) {
                tvAdminTitle.setText("ZONA SUPERADMINISTRADOR");
                btnAdmin.setText("SELECCIONAR EMPRESA");
                map.setVisibility(View.GONE);
            } else {
                tvAdminTitle.setText("ZONA ADMINISTRADOR (Cargando...)");
            }

            btnAdmin.setOnClickListener(v -> {
                if (sessionManager.getRol().equalsIgnoreCase("Superadministrador")) {
                    mostrarDialogoEmpresas();
                } else {
                    Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_adminEmpleadosFragment);
                }
            });
        }

        // Pedir permiso de notificaciones para Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), mensaje -> Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show());

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnEntrada.setEnabled(!isLoading);
            btnSalida.setEnabled(!isLoading);
        });

        cargarPerfilEmpresa();
    }

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

            map.getController().setCenter(puntoEmpresa);

            Marker markerEmpresa = new Marker(map);
            markerEmpresa.setPosition(puntoEmpresa);
            markerEmpresa.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            markerEmpresa.setTitle(empresa.getNombre());
            map.getOverlays().add(markerEmpresa);

            Polygon circulo = new Polygon();
            List<GeoPoint> puntosCirculo = Polygon.pointsAsCircle(puntoEmpresa, radioMetros);
            circulo.setPoints(puntosCirculo);
            circulo.setFillColor(Color.argb(50, 0, 0, 255));
            circulo.setStrokeColor(Color.BLUE);
            circulo.setStrokeWidth(2.0f);
            map.getOverlays().add(circulo);

            if (checkPermission()) {
                MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
                myLocationOverlay.enableMyLocation();
                map.getOverlays().add(myLocationOverlay);
            }

            if (sessionManager.isAdmin() && !sessionManager.getRol().equalsIgnoreCase("Superadministrador")) {
                TextView tvAdminTitle = getView().findViewById(R.id.tvAdminTitle);
                tvAdminTitle.setText("ZONA ADMIN - " + empresa.getNombre().toUpperCase());
            }

            map.invalidate();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
                actualizarMapa();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            // Encendemos el radar NFC
            android.os.Bundle options = new android.os.Bundle();
            options.putInt(android.nfc.NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);
            nfcAdapter.enableReaderMode(requireActivity(), nfcCallback,
                    android.nfc.NfcAdapter.FLAG_READER_NFC_A |
                            android.nfc.NfcAdapter.FLAG_READER_NFC_B |
                            android.nfc.NfcAdapter.FLAG_READER_NFC_F |
                            android.nfc.NfcAdapter.FLAG_READER_NFC_V |
                            android.nfc.NfcAdapter.FLAG_READER_NFC_BARCODE,
                    options);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            // Apagamos el radar NFC al salir de la app
            nfcAdapter.disableReaderMode(requireActivity());
        }
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

    private void mostrarDialogoEmpresas() {
        progressBar.setVisibility(View.VISIBLE);
        String token = sessionManager.getToken();
        RetrofitClient.getInstance().getMyApi().getEmpresasAdmin(token).enqueue(new Callback<List<Empresa>>() {
            @Override
            public void onResponse(Call<List<Empresa>> call, Response<List<Empresa>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Empresa> lista = response.body();
                    String[] nombres = new String[lista.size()];
                    for (int i = 0; i < lista.size(); i++) nombres[i] = lista.get(i).getNombre();

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Selecciona Empresa a Gestionar")
                            .setItems(nombres, (dialog, which) -> {
                                Bundle bundle = new Bundle();
                                bundle.putInt("empresa_id", lista.get(which).getIdEmpresa());
                                Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_adminEmpleadosFragment, bundle);
                            }).show();
                }
            }
            @Override
            public void onFailure(Call<List<Empresa>> call, Throwable t) { progressBar.setVisibility(View.GONE); }
        });
    }

    // --- LÓGICA NFC REAL ---
    private final android.nfc.NfcAdapter.ReaderCallback nfcCallback = tag -> {
        // 1. Extraemos el número de serie (UID) del chip NFC
        byte[] id = tag.getId();
        String nfcUid = bytesToHex(id);

        // 2. Volvemos al hilo principal de la pantalla para mostrar cosas
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "¡Tarjeta NFC detectada! UID: " + nfcUid, Toast.LENGTH_SHORT).show();
            procesarFichajeNFC(nfcUid);
        });
    };

    // Función auxiliar para convertir los bytes del chip en texto legible
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void procesarFichajeNFC(String uidTarjeta) {
        // Aquí es donde mandaremos el UID al servidor para decirle "Oye, ficha a este tío"
        android.util.Log.d("NFC_TEST", "El servidor va a procesar la tarjeta: " + uidTarjeta);
        Toast.makeText(getContext(), "Procesando tarjeta: " + uidTarjeta, Toast.LENGTH_SHORT).show();
    }
}