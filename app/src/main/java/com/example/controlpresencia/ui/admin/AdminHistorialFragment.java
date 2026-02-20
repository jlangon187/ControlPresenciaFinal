package com.example.controlpresencia.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.Fichaje;
import com.example.controlpresencia.data.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHistorialFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvTitulo;
    private SessionManager sessionManager;
    private int idTrabajador;
    private String nombreTrabajador;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        // Reutilizamos el layout de fichajes normal o creamos uno nuevo con un TextView de título arriba
        return inflater.inflate(R.layout.fragment_admin_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sessionManager = new SessionManager(requireContext());
        recyclerView = view.findViewById(R.id.rvAdminHistorial);
        tvTitulo = view.findViewById(R.id.tvTituloHistorialAdmin);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Recuperar argumentos
        if (getArguments() != null) {
            idTrabajador = getArguments().getInt("id_trabajador");
            nombreTrabajador = getArguments().getString("nombre_trabajador");
            tvTitulo.setText("Historial de " + nombreTrabajador);
        }

        cargarHistorial();
    }

    private void cargarHistorial() {
        String token = sessionManager.getToken();
        RetrofitClient.getInstance().getMyApi().getHistorialAdmin(token, idTrabajador).enqueue(new Callback<List<Fichaje>>() {
            @Override
            public void onResponse(Call<List<Fichaje>> call, Response<List<Fichaje>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminHistorialAdapter adapter = new AdminHistorialAdapter(response.body(), fichaje -> {
                        if (fichaje.getLatitud() != null && fichaje.getLongitud() != null) {
                            Bundle bundle = new Bundle();
                            bundle.putDouble("lat", fichaje.getLatitud());
                            bundle.putDouble("lng", fichaje.getLongitud());
                            bundle.putString("nombre", nombreTrabajador);

                            bundle.putString("fecha", fichaje.getFecha());
                            bundle.putString("hora_entrada", fichaje.getHoraEntradaFormateada());
                            bundle.putString("hora_salida", fichaje.getHoraSalidaFormateada());

                            Navigation.findNavController(getView()).navigate(R.id.action_adminHistorial_to_mapDetail, bundle);
                        } else {
                            Toast.makeText(getContext(), "Sin coordenadas GPS", Toast.LENGTH_SHORT).show();
                        }
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "No hay registros o error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Fichaje>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}