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

public class AdminIncidenciasFragment extends Fragment {

    private SessionManager sessionManager;
    private RecyclerView rvIncidencias;
    private ProgressBar progressBar;
    private IncidenciasAdapter adapter;
    private int empresaId = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_incidencias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        rvIncidencias = view.findViewById(R.id.rvIncidencias);
        progressBar = view.findViewById(R.id.progressBarIncidencias);

        rvIncidencias.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IncidenciasAdapter(new ArrayList<>());
        rvIncidencias.setAdapter(adapter);

        if (getArguments() != null) {
            empresaId = getArguments().getInt("empresa_id", -1);
        }

        if (empresaId != -1) {
            cargarIncidencias(empresaId);
        } else {
            Toast.makeText(getContext(), "Error: No se recibió la empresa", Toast.LENGTH_SHORT).show();
        }

        View btnVolver = view.findViewById(R.id.btnVolverAdminIncidencias);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigateUp());
        }
    }

    private void cargarIncidencias(int idEmpresa) {
        progressBar.setVisibility(View.VISIBLE);
        String token = sessionManager.getToken();

        RetrofitClient.getInstance().getMyApi().getIncidenciasEmpresa(token, idEmpresa).enqueue(new Callback<List<Incidencia>>() {
            @Override
            public void onResponse(Call<List<Incidencia>> call, Response<List<Incidencia>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(getContext(), "La base de datos devolvió 0 incidencias", Toast.LENGTH_LONG).show();
                    } else {
                        adapter.setLista(response.body());
                    }
                } else {
                    Toast.makeText(getContext(), "Error del servidor: Código " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Incidencia>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Fallo en la App: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private class IncidenciasAdapter extends RecyclerView.Adapter<IncidenciasAdapter.ViewHolder> {
        private List<Incidencia> lista;

        public IncidenciasAdapter(List<Incidencia> lista) {
            this.lista = lista;
        }

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

            holder.tvAsunto.setText(incidencia.getTitulo() != null ? incidencia.getTitulo() : "Sin Asunto");
            holder.tvDescripcion.setText(incidencia.getDescripcion());

            String empleado = incidencia.getEmpleadoNombre() != null ? incidencia.getEmpleadoNombre() : "Desconocido";
            String fecha = incidencia.getFecha() != null ? incidencia.getFecha() : "";
            String fechaformateada = fecha.substring(0, 10);
            String[] partes = fechaformateada.split("-");
            if (partes.length == 3) {
                fechaformateada = fecha.substring(11, 16) + " - " + partes[2] + "/" + partes[1] + "/" + partes[0];
            }
            holder.tvEmpleadoFecha.setText(empleado + "\n" + fechaformateada);
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