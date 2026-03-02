package com.example.controlpresencia.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.Incidencia;
import com.example.controlpresencia.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Pantalla para que el administrador vea las quejas o problemas (incidencias) que han enviado los empleados.
public class AdminIncidenciasFragment extends Fragment {

    private SessionManager sessionManager;
    private RecyclerView rvIncidencias;
    private ProgressBar progressBar;
    private IncidenciasAdapter adapter;
    private int empresaId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        // Inflamos el diseño de la lista de incidencias para el administrador.
        return inflater.inflate(R.layout.fragment_admin_incidencias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        sessionManager = new SessionManager(requireContext());
        rvIncidencias = view.findViewById(R.id.rvIncidencias);
        progressBar = view.findViewById(R.id.progressBarIncidencias);

        // Configuramos la lista y le ponemos un adaptador vacío al principio.
        rvIncidencias.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IncidenciasAdapter(new ArrayList<>());
        rvIncidencias.setAdapter(adapter);

        // Pillamos el ID de la empresa de los argumentos para saber de quién cargar las incidencias.
        if (getArguments() != null) {
            empresaId = getArguments().getInt("empresa_id", -1);
        }

        if (empresaId != -1) {
            cargarIncidencias(empresaId);
        } else {
            Toast.makeText(getContext(), "Error: No se recibió la empresa", Toast.LENGTH_SHORT).show();
        }

        // Botón para volver atrás.
        View btnVolver = view.findViewById(R.id.btnVolverAdminIncidencias);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigateUp());
        }
    }

    // Llama a la API para traer todas las incidencias de una empresa concreta.
    private void cargarIncidencias(int idEmpresa) {
        progressBar.setVisibility(View.VISIBLE);
        String token = sessionManager.getToken();

        RetrofitClient.getInstance().getMyApi().getIncidenciasEmpresa(token, idEmpresa).enqueue(new Callback<List<Incidencia>>() {
            @Override
            public void onResponse(Call<List<Incidencia>> call, Response<List<Incidencia>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(getContext(), "No hay incidencias registradas", Toast.LENGTH_LONG).show();
                    } else {
                        // Si hay datos, actualizamos el adaptador para que se vean en la lista.
                        adapter.setLista(response.body());
                    }
                } else {
                    Toast.makeText(getContext(), "Error del servidor: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Incidencia>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adaptador interno para pintar cada incidencia en su fila correspondiente.
    private class IncidenciasAdapter extends RecyclerView.Adapter<IncidenciasAdapter.ViewHolder> {
        private List<Incidencia> lista;

        public IncidenciasAdapter(List<Incidencia> lista) {
            this.lista = lista;
        }

        // Método para cambiar la lista entera de golpe cuando llegan datos nuevos.
        public void setLista(List<Incidencia> nuevaLista) {
            this.lista = nuevaLista;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Incidencia incidencia = lista.get(position);

            // Rellenamos el título y la descripción.
            holder.tvAsunto.setText(incidencia.getTitulo() != null ? incidencia.getTitulo() : "Sin Asunto");
            holder.tvDescripcion.setText(incidencia.getDescripcion());

            // Formateamos la fecha que viene del servidor para que se lea mejor (HH:mm - DD/MM/YYYY).
            String empleado = incidencia.getEmpleadoNombre() != null ? incidencia.getEmpleadoNombre() : "Desconocido";
            String fecha = incidencia.getFecha() != null ? incidencia.getFecha() : "";
            
            try {
                String fechaformateada = fecha.substring(0, 10);
                String[] partes = fechaformateada.split("-");
                if (partes.length == 3) {
                    fechaformateada = fecha.substring(11, 16) + " - " + partes[2] + "/" + partes[1] + "/" + partes[0];
                }
                holder.tvEmpleadoFecha.setText(empleado + "\n" + fechaformateada);
            } catch (Exception e) {
                holder.tvEmpleadoFecha.setText(empleado + "\n" + fecha);
            }
        }

        @Override
        public int getItemCount() {
            return lista != null ? lista.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAsunto, tvEmpleadoFecha, tvDescripcion;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAsunto = itemView.findViewById(R.id.tvAsuntoIncidencia);
                tvEmpleadoFecha = itemView.findViewById(R.id.tvEmpleadoFecha);
                tvDescripcion = itemView.findViewById(R.id.tvDescripcionIncidencia);
            }
        }
    }
}