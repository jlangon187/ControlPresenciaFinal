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

import com.example.controlpresencia.MainActivity;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.AdminStatsResponse;
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

// Fragment principal: muestra el panel del trabajador o el panel de admin/superadmin,
// permite fichar (entrada por GPS, salida normal) y fichar con NFC.
// También pinta un mapa con la ubicación/radio de la empresa.
public class HomeFragment extends Fragment {

    // --- VARIABLES ---

    // Estado global para alternar entre "modo trabajador" y "modo admin" en el panel
    private static boolean isModoAdminActivo = false;

    private HomeViewModel viewModel;
    private SessionManager sessionManager;

    // UI
    private ProgressBar progressBar;
    private Button btnEntrada, btnSalida, btnReportarIncidencia;
    private MapView map;

    // Datos del usuario logueado (perfil con empresa, etc.)
    private User usuarioActual;

    // Cliente de ubicación (GPS)
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 100;

    // NFC
    private android.nfc.NfcAdapter nfcAdapter;

    // En superadmin: empresa sobre la que se está trabajando (se selecciona en un diálogo)
    private Empresa empresaSeleccionadaSuperadmin = null;

    // =========================================================================
    // CICLO DE VIDA
    // =========================================================================

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Configuración necesaria para OSMDroid (mapa) usando SharedPreferences
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicialización de helpers: sesión, ViewModel, GPS y NFC
        sessionManager = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(requireContext());

        // Vincular Vistas
        Button btnHistorial = view.findViewById(R.id.btnVerHistorial);
        progressBar = view.findViewById(R.id.progressBarHome);
        btnEntrada = view.findViewById(R.id.btnEntrada);
        btnSalida = view.findViewById(R.id.btnSalida);
        btnReportarIncidencia = view.findViewById(R.id.btnReportarIncidencia);
        map = view.findViewById(R.id.map);

        // Configurar Mapa (OSMDroid)
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getController().setZoom(18.0);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(false);
        map.setOnTouchListener((v, event) -> true);

        // Saludo usando el nombre guardado en sesión
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        String nombre = sessionManager.getNombre();
        if (nombre != null && !nombre.isEmpty()) {
            String nombreSolo = nombre.split(" ")[0];
            tvWelcome.setText("Hola, " + nombreSolo);
        }

        // Fecha formateada en español para mostrar en el panel
        TextView tvAdminFechaLocal = view.findViewById(R.id.tvAdminFechaLocal);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, d 'de' MMMM", new java.util.Locale("es", "ES"));
        String fechaBonita = sdf.format(new java.util.Date());
        fechaBonita = fechaBonita.substring(0, 1).toUpperCase() + fechaBonita.substring(1);
        tvAdminFechaLocal.setText(fechaBonita);

        // Listeners de botones de navegación y acciones
        configurarBotones(view, btnHistorial);

        // Monta la UI dependiendo del rol (trabajador / admin / superadmin)
        configurarPanelRoles(view);

        // Pedir permiso de notificaciones para Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Observa mensajes y estado de carga para bloquear botones y mostrar loader
        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), mensaje -> Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show());

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnEntrada.setEnabled(!isLoading);
            btnSalida.setEnabled(!isLoading);
        });

        // Llama a la API para cargar el perfil (y con ello empresa/ubicación)
        cargarPerfilEmpresa();
    }

    // Metodo onResune para NFC
    @Override
    public void onResume() {
        super.onResume();

        // Activa el lector NFC mientras el fragment está en primer plano
        if (nfcAdapter != null) {
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

        // Si la Activity ha recibido un intent NFC, lo procesa para fichar con el UID
        android.content.Intent intent = requireActivity().getIntent();
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            if (android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                    android.nfc.NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
                    android.nfc.NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

                android.nfc.Tag tag = intent.getParcelableExtra(android.nfc.NfcAdapter.EXTRA_TAG);
                if (tag != null) {
                    String nfcUid = bytesToHex(tag.getId());

                    Toast.makeText(getContext(), "Fichando...", Toast.LENGTH_SHORT).show();
                    procesarFichajeNFC(nfcUid);

                    // Se limpia la acción para no re-procesar el mismo intent al volver a entrar
                    intent.setAction(android.content.Intent.ACTION_MAIN);
                }
            }
        }
    }

    // Metodo onPause para NFC
    @Override
    public void onPause() {
        super.onPause();
        // Desactiva el lector NFC para ahorrar recursos y evitar lecturas fuera de contexto
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(requireActivity());
        }
    }

    // =========================================================================
    // CONFIGURACIÓN DE VISTAS
    // =========================================================================

    private void configurarBotones(View view, Button btnHistorial) {
        // Navegación
        btnHistorial.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_fichajesFragment));
        btnReportarIncidencia.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_incidenciaFragment));

        // Botón Fichar Entrada: exige GPS y envía lat/long al ViewModel
        btnEntrada.setOnClickListener(v -> {
            if (checkPermission()) {
                obtenerUbicacionYFichar();
            } else {
                pedirPermisoGPS();
            }
        });

        // Botón Fichar Salida: solo envía el token al ViewModel
        btnSalida.setOnClickListener(v -> {
            String token = sessionManager.getToken();
            if (token != null) {
                viewModel.ficharSalida(token); // Directamente lanza la salida
            } else {
                Toast.makeText(getContext(), "Error de sesión", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón Ver Horario
        view.findViewById(R.id.btnVerHorario).setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_horarioFragment));

        // Botón Cerrar Sesión con confirmación (borra sesión y vuelve al login)
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Estás seguro de que deseas salir de tu cuenta?")
                    .setPositiveButton("Sí, salir", (dialog, which) -> {
                        logout();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show()
                    .getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#EF4444"));
        });
    }

    private void configurarPanelRoles(View view) {
        // Elementos comunes del panel
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        MaterialButton btnAdminSwitch = view.findViewById(R.id.btnAdminPanel);
        View cardAdminDashboard = view.findViewById(R.id.cardAdminDashboard);
        TextView tvAdminDashboardTitle = view.findViewById(R.id.tvAdminDashboardTitle);
        MaterialButton btnAdminAccion1 = view.findViewById(R.id.btnAdminAccion1);
        MaterialButton btnAdminAccion2 = view.findViewById(R.id.btnAdminAccion2);
        MaterialButton btnAdminAccion3 = view.findViewById(R.id.btnAdminUbicacion);

        androidx.constraintlayout.widget.Group groupTrabajador = view.findViewById(R.id.groupTrabajador);

        // Si el usuario es admin, se muestran opciones de administración
        if (sessionManager.isAdmin()) {

            // Superadmin: trabaja "a nivel global" eligiendo empresa
            if (sessionManager.getRol().equalsIgnoreCase("Superadministrador")) {
                // ==========================================
                // MODO SUPERADMINISTRADOR (Dinámico)
                // ==========================================
                tvStatus.setText("Panel de Control Global");
                groupTrabajador.setVisibility(View.GONE);
                cardAdminDashboard.setVisibility(View.VISIBLE);

                btnAdminSwitch.setVisibility(View.VISIBLE);
                btnAdminSwitch.setText("SELECCIONAR EMPRESA");
                btnAdminSwitch.setBackgroundColor(Color.parseColor("#10B981")); // Color Verde

                // Abre un diálogo para elegir empresa y cargar acciones con ese id
                btnAdminSwitch.setOnClickListener(v -> mostrarDialogoSeleccionEmpresaGlobal(view));

                // Pinta el dashboard en función de si ya hay empresa seleccionada o no
                actualizarUiSuperadmin(view);

            } else {
                // ==========================================
                // MODO ADMINISTRADOR (EMPRESA ESPECÍFICA)
                // ==========================================
                btnAdminSwitch.setVisibility(View.VISIBLE);

                // Acciones del admin (empleados, incidencias y configuración mapa)
                btnAdminAccion1.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_adminEmpleadosFragment));
                btnAdminAccion2.setOnClickListener(v -> {
                    if (usuarioActual != null && usuarioActual.getEmpresa() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("empresa_id", usuarioActual.getEmpresa().getIdEmpresa());
                        Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_adminIncidenciasFragment, bundle);
                    }
                });
                btnAdminAccion3.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_configMapaFragment));

                // Si está activo el modo admin, cambia UI a dashboard
                if (isModoAdminActivo) {
                    btnAdminSwitch.setText("MODO TRABAJADOR");
                    btnAdminSwitch.setBackgroundColor(Color.parseColor("#3B82F6"));
                    tvStatus.setText("Panel del Administrador");
                    groupTrabajador.setVisibility(View.GONE);
                    cardAdminDashboard.setVisibility(View.VISIBLE);

                    // Trae estadísticas (activos/ausencias)
                    cargarEstadisticasAdmin(view);

                    // Título con nombre de la empresa del usuario
                    String nombreEmpresa = (usuarioActual != null && usuarioActual.getEmpresa() != null)
                            ? usuarioActual.getEmpresa().getNombre().toUpperCase() : "EMPRESA";
                    tvAdminDashboardTitle.setText("Empresa:\n" + nombreEmpresa);

                    // Textos de botones del panel admin
                    btnAdminAccion1.setText("GESTIONAR MIS EMPLEADOS");
                    btnAdminAccion2.setText("INCIDENCIAS DE MI EMPRESA");
                    btnAdminAccion3.setText("CONFIGURAR UBICACIÓN"); // NUEVO
                } else {
                    // Modo trabajador
                    btnAdminSwitch.setText("MODO ADMIN");
                    btnAdminSwitch.setBackgroundColor(Color.parseColor("#EA580C"));
                    tvStatus.setText("Panel del Trabajador");
                    groupTrabajador.setVisibility(View.VISIBLE);
                    cardAdminDashboard.setVisibility(View.GONE);
                }

                // Botón que alterna entre modo trabajador y modo admin
                btnAdminSwitch.setOnClickListener(v -> {
                    isModoAdminActivo = !isModoAdminActivo;

                    if (isModoAdminActivo) {
                        btnAdminSwitch.setText("MODO TRABAJADOR");
                        btnAdminSwitch.setBackgroundColor(Color.parseColor("#3B82F6"));
                        tvStatus.setText("Panel del Administrador");
                        groupTrabajador.setVisibility(View.GONE);
                        cardAdminDashboard.setVisibility(View.VISIBLE);

                        cargarEstadisticasAdmin(view);

                        String nombreEmpresa = (usuarioActual != null && usuarioActual.getEmpresa() != null)
                                ? usuarioActual.getEmpresa().getNombre().toUpperCase() : "EMPRESA";
                        tvAdminDashboardTitle.setText("Empresa:\n" + nombreEmpresa);

                        btnAdminAccion1.setText("GESTIONAR MIS EMPLEADOS");
                        btnAdminAccion2.setText("INCIDENCIAS DE MI EMPRESA");
                        btnAdminAccion3.setText("CONFIGURAR UBICACIÓN"); // NUEVO
                    } else {
                        btnAdminSwitch.setText("MODO ADMIN");
                        btnAdminSwitch.setBackgroundColor(Color.parseColor("#EA580C"));
                        tvStatus.setText("Panel del Trabajador");
                        groupTrabajador.setVisibility(View.VISIBLE);
                        cardAdminDashboard.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            // Usuario normal: solo panel trabajador
            cardAdminDashboard.setVisibility(View.GONE);
            groupTrabajador.setVisibility(View.VISIBLE);
            btnAdminSwitch.setVisibility(View.GONE);
            tvStatus.setText("Panel del Trabajador");
        }
    }

    private void logout() {
        // Borra preferencias de sesión y navega al login limpiando backstack
        sessionManager.clearSession();
        View view = getView();
        if (view != null) {
            androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build();
            Navigation.findNavController(view).navigate(R.id.loginFragment, null, navOptions);
        }
    }

    // =========================================================================
    // MAPAS Y GPS (Solo Entrada)
    // =========================================================================

    private void actualizarMapa() {
        // Pinta empresa + radio y, si hay permiso, la posición del usuario
        if (map == null || usuarioActual == null || usuarioActual.getEmpresa() == null) return;

        Empresa empresa = usuarioActual.getEmpresa();
        if (empresa.getLatitud() != null && empresa.getLongitud() != null) {
            GeoPoint puntoEmpresa = new GeoPoint(empresa.getLatitud(), empresa.getLongitud());
            int radioMetros = empresa.getRadio() != null ? empresa.getRadio() : 100;

            map.getController().setCenter(puntoEmpresa);

            // Marcador en la ubicación de la empresa
            Marker markerEmpresa = new Marker(map);
            markerEmpresa.setPosition(puntoEmpresa);
            markerEmpresa.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            markerEmpresa.setTitle(empresa.getNombre());
            map.getOverlays().add(markerEmpresa);

            // Círculo de radio permitido para fichar
            Polygon circulo = new Polygon();
            List<GeoPoint> puntosCirculo = Polygon.pointsAsCircle(puntoEmpresa, radioMetros);
            circulo.setPoints(puntosCirculo);
            circulo.setFillColor(Color.argb(50, 0, 0, 255));
            circulo.setStrokeColor(Color.BLUE);
            circulo.setStrokeWidth(2.0f);
            map.getOverlays().add(circulo);

            // Overlay de "mi ubicación" si hay permisos de GPS
            if (checkPermission()) {
                MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
                myLocationOverlay.enableMyLocation();
                map.getOverlays().add(myLocationOverlay);
            }

            map.invalidate();
        }
    }

    private boolean checkPermission() {
        // Comprueba permiso de ubicación precisa
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void pedirPermisoGPS() {
        // Pide permiso de GPS al usuario
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si da permiso, se intenta fichar entrada y se actualiza el mapa
                obtenerUbicacionYFichar();
                actualizarMapa();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacionYFichar() {
        // Obtiene la última ubicación conocida y manda la entrada con lat/long
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

    // =========================================================================
    // NFC Y LLAMADAS DE RED (API)
    // =========================================================================

    private void cargarPerfilEmpresa() {
        // Llama a la API para obtener el perfil del usuario (incluye su empresa)
        String token = sessionManager.getToken();
        RetrofitClient.getInstance().getMyApi().getPerfil(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    usuarioActual = response.body();
                    // Con la empresa cargada, se puede pintar el mapa con ubicación/radio
                    actualizarMapa();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) { }
        });
    }

    // Callback de lectura NFC (modo reader)
    private final android.nfc.NfcAdapter.ReaderCallback nfcCallback = tag -> {
        byte[] id = tag.getId();
        String nfcUid = bytesToHex(id);

        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "¡Tarjeta NFC detectada! UID: " + nfcUid, Toast.LENGTH_SHORT).show();
            // Envía el UID al backend para registrar fichaje por NFC
            procesarFichajeNFC(nfcUid);
        });
    };

    private String bytesToHex(byte[] bytes) {
        // Convierte el UID en bytes a string hexadecimal (formato típico para NFC)
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void procesarFichajeNFC(String uidTarjeta) {
        // Envía al backend el UID de la tarjeta NFC para registrar el fichaje
        String token = sessionManager.getToken();
        if (token == null) return;

        progressBar.setVisibility(View.VISIBLE);
        com.example.controlpresencia.data.model.FichajeRequest request = new com.example.controlpresencia.data.model.FichajeRequest(uidTarjeta);

        RetrofitClient.getInstance().getMyApi().ficharNFC(token, request).enqueue(new Callback<com.example.controlpresencia.data.model.FichajeResponse>() {
            @Override
            public void onResponse(Call<com.example.controlpresencia.data.model.FichajeResponse> call, Response<com.example.controlpresencia.data.model.FichajeResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // Mensaje OK devuelto por el backend
                    Toast.makeText(getContext(), "✅ " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    // Intenta leer el mensaje de error desde el body (JSON) si existe
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            org.json.JSONObject jsonObject = new org.json.JSONObject(errorStr);
                            Toast.makeText(getContext(), "⚠️ " + jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "❌ Tarjeta rechazada", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "❌ Error al fichar por NFC", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<com.example.controlpresencia.data.model.FichajeResponse> call, Throwable t) {
                // Error de red / servidor no accesible
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "⚠️ Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarUiSuperadmin(View view) {
        // Ajusta el dashboard del superadmin en función de si ya hay empresa seleccionada
        TextView tvAdminDashboardTitle = view.findViewById(R.id.tvAdminDashboardTitle);
        MaterialButton btnAdminAccion1 = view.findViewById(R.id.btnAdminAccion1);
        MaterialButton btnAdminAccion2 = view.findViewById(R.id.btnAdminAccion2);
        MaterialButton btnAdminAccion3 = view.findViewById(R.id.btnAdminUbicacion);

        btnAdminAccion1.setText("GESTIONAR MIS EMPLEADOS");
        btnAdminAccion2.setText("INCIDENCIAS DE MI EMPRESA");
        btnAdminAccion3.setText("CONFIGURAR UBICACIÓN");

        // Si no hay empresa elegida, las acciones no hacen nada y se avisa
        if (empresaSeleccionadaSuperadmin == null) {
            tvAdminDashboardTitle.setText("Centro Global\n(Selecciona empresa arriba)");
            tvAdminDashboardTitle.setTextSize(16f); // Más pequeño para que quepa

            View.OnClickListener alertaSeleccion = v ->
                    Toast.makeText(getContext(), "☝️ Selecciona una empresa en el botón verde primero", Toast.LENGTH_SHORT).show();

            btnAdminAccion1.setOnClickListener(alertaSeleccion);
            btnAdminAccion2.setOnClickListener(alertaSeleccion);
            btnAdminAccion3.setOnClickListener(alertaSeleccion);

        } else {
            // Con empresa seleccionada, se navega pasando empresa_id en Bundle
            tvAdminDashboardTitle.setText("Empresa:\n" + empresaSeleccionadaSuperadmin.getNombre().toUpperCase());
            tvAdminDashboardTitle.setTextSize(18f);

            btnAdminAccion1.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("empresa_id", empresaSeleccionadaSuperadmin.getIdEmpresa());
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_adminEmpleadosFragment, bundle);
            });

            btnAdminAccion2.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("empresa_id", empresaSeleccionadaSuperadmin.getIdEmpresa());
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_adminIncidenciasFragment, bundle);
            });

            btnAdminAccion3.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("empresa_id", empresaSeleccionadaSuperadmin.getIdEmpresa());
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_configMapaFragment, bundle);
            });

            // Carga estadísticas para la empresa seleccionada
            cargarEstadisticasAdmin(view);
        }
    }

    private void mostrarDialogoSeleccionEmpresaGlobal(View mainView) {
        // Para superadmin: trae lista de empresas y deja elegir una
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
                            .setTitle("Trabajar con la empresa:")
                            .setItems(nombres, (dialog, which) -> {
                                // Guarda la empresa elegida y refresca el dashboard
                                empresaSeleccionadaSuperadmin = lista.get(which);
                                actualizarUiSuperadmin(mainView);
                            }).show();
                } else {
                    Toast.makeText(getContext(), "Error al cargar lista de empresas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Empresa>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarEstadisticasAdmin(View view) {
        // Carga estadísticas del panel admin: activos y ausencias
        TextView tvStatActivos = view.findViewById(R.id.tvAdminStatActivos);
        TextView tvStatAusencias = view.findViewById(R.id.tvAdminStatAusencias);

        String token = sessionManager.getToken();
        if (token == null) return;

        // Si es superadmin, se envía empresa_id (según selección).
        // Si es admin normal, se envía null y el backend entiende la empresa del usuario.
        Integer empresaIdAEnviar = null;
        if (sessionManager.getRol().equalsIgnoreCase("Superadministrador")) {
            if (empresaSeleccionadaSuperadmin == null) {
                tvStatActivos.setText("-");
                tvStatAusencias.setText("-");
                return;
            }
            empresaIdAEnviar = empresaSeleccionadaSuperadmin.getIdEmpresa();
        }

        RetrofitClient.getInstance().getMyApi().getAdminStats(token, empresaIdAEnviar).enqueue(new Callback<AdminStatsResponse>() {
            @Override
            public void onResponse(Call<AdminStatsResponse> call, Response<AdminStatsResponse> response) {
                if (getView() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    tvStatActivos.setText(String.valueOf(response.body().getActivos()));
                    tvStatAusencias.setText(String.valueOf(response.body().getAusencias()));
                } else {
                    tvStatActivos.setText("-");
                    tvStatAusencias.setText("-");
                }
            }

            @Override
            public void onFailure(Call<AdminStatsResponse> call, Throwable t) {
                // Si falla la red, se marca con X para indicar que no se pudo cargar
                if (getView() != null) {
                    tvStatActivos.setText("X");
                    tvStatAusencias.setText("X");
                }
            }
        });
    }
}