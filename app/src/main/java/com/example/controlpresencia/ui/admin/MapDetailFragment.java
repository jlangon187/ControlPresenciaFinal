package com.example.controlpresencia.ui.admin;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.controlpresencia.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

// Esta pantalla sirve para ver en un mapa dónde ha fichado exactamente un trabajador.
public class MapDetailFragment extends Fragment {

    // El objeto del mapa que vamos a mostrar.
    private MapView map;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        // Cargamos la configuración de OpenStreetMap para que el mapa funcione bien.
        Configuration.getInstance().load(getContext(), PreferenceManager.getDefaultSharedPreferences(getContext()));
        return inflater.inflate(R.layout.fragment_map_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        // Configuramos el mapa: tipo de mapa y que se pueda mover con los dedos.
        map = view.findViewById(R.id.mapdetail);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Referencias a los textos donde pondremos el nombre, la fecha y las horas.
        TextView tvNombre = view.findViewById(R.id.tvMapNombre);
        TextView tvFecha = view.findViewById(R.id.tvMapFecha);
        TextView tvHoras = view.findViewById(R.id.tvMapHoras);

        // Botón para volver a la pantalla anterior.
        View btnVolver = view.findViewById(R.id.btnVolverMapa);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigateUp());
        }

        // Sacamos los datos del trabajador y su ubicación que nos han pasado al abrir este fragment.
        if (getArguments() != null) {
            double lat = getArguments().getDouble("lat");
            double lng = getArguments().getDouble("lng");
            String nombre = getArguments().getString("nombre");

            String fecha = getArguments().getString("fecha");
            String horaEntrada = getArguments().getString("hora_entrada");
            String horaSalida = getArguments().getString("hora_salida");

            // Rellenamos los textos con la información del fichaje.
            tvNombre.setText(nombre);
            tvFecha.setText("Fecha del Fichaje: " + (fecha != null ? fecha : "Desconocida"));
            tvHoras.setText("🟢 Entrada: " + (horaEntrada != null ? horaEntrada : "--:--") +
                    "   |   🔴 Salida: " + (horaSalida != null ? horaSalida : "--:--"));

            // Centramos el mapa en las coordenadas del fichaje y ponemos un marcador.
            GeoPoint punto = new GeoPoint(lat, lng);
            map.getController().setZoom(18.0);
            map.getController().setCenter(punto);

            Marker marker = new Marker(map);
            marker.setPosition(punto);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle("Ubicación de " + nombre);
            map.getOverlays().add(marker);
        }
    }

    // Estos métodos son necesarios para que el mapa se gestione bien cuando abres o cierras la app.
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
}