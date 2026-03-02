package com.example.controlpresencia.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.User;
import com.example.controlpresencia.data.network.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Pantalla donde el administrador puede ver la lista de todos los empleados.
public class AdminEmpleadosFragment extends Fragment {

    private RecyclerView recyclerView;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        // Ponemos el diseño de la pantalla de lista de empleados para el admin.
        return inflater.inflate(R.layout.fragment_admin_empleados, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sessionManager = new SessionManager(requireContext());
        recyclerView = view.findViewById(R.id.rvAdminEmpleados);
        // Configuramos la lista para que sea vertical.
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pedimos la lista al servidor nada más abrir la pantalla.
        cargarEmpleados();

        // Configuración del botón para ir hacia atrás.
        View btnVolver = view.findViewById(R.id.btnVolverAdminEmpleados);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigateUp());
        }

    }

    // Llama a la API para pillar todos los empleados, filtrando por empresa si hace falta.
    private void cargarEmpleados() {
        String token = sessionManager.getToken();

        // Miramos si nos han pasado un filtro de empresa por los argumentos.
        Integer empresaId = null;
        if (getArguments() != null && getArguments().containsKey("empresa_id")) {
            empresaId = getArguments().getInt("empresa_id");
        }

        RetrofitClient.getInstance().getMyApi().getEmpleadosAdmin(token, empresaId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Si el servidor nos da la lista, creamos el adaptador para mostrarla.
                    AdminEmpleadosAdapter adapter = new AdminEmpleadosAdapter(response.body(), empleado -> {
                        // Al pulsar en un empleado, navegamos a su historial de fichajes.
                        Bundle bundle = new Bundle();
                        bundle.putInt("id_trabajador", empleado.getIdTrabajador());
                        bundle.putString("nombre_trabajador", empleado.getNombreCompleto());
                        Navigation.findNavController(getView()).navigate(R.id.action_adminEmpleados_to_adminHistorial, bundle);
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    // Si falla la petición.
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // Si hay un error de red (no hay internet, servidor caído...).
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}