package com.example.controlpresencia.ui.admin;

import android.graphics.Color;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlpresencia.R;
import com.example.controlpresencia.data.local.SessionManager;
// Importa tu modelo real de Incidencia (ajusta el paquete si es distinto)
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

        // Configurar la lista
        rvIncidencias.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IncidenciasAdapter(new ArrayList<>());
        rvIncidencias.setAdapter(adapter);

        // 1. Recoger el ID de la empresa que nos manda el HomeFragment
        if (getArguments() != null) {
            empresaId = getArguments().getInt("empresa_id", -1);
        }

        // 2. Cargar los datos
        if (empresaId != -1) {
            cargarIncidencias(empresaId);
        } else {
            Toast.makeText(getContext(), "Error: No se recibió la empresa", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarIncidencias(int idEmpresa) {
        progressBar.setVisibility(View.VISIBLE);
        String token = sessionManager.getToken();

        // IMPORTANTE: Asegúrate de que en tu MyApi.java tienes un endpoint para esto.
        // Ejemplo: getIncidenciasEmpresa(token, idEmpresa)
        RetrofitClient.getInstance().getMyApi().getIncidenciasEmpresa(token, idEmpresa).enqueue(new Callback<List<Incidencia>>() {
            @Override
            public void onResponse(Call<List<Incidencia>> call, Response<List<Incidencia>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setLista(response.body());
                } else {
                    Toast.makeText(getContext(), "No hay incidencias o hubo un error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Incidencia>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================================
    // ADAPTADOR INTERNO PARA EL RECYCLERVIEW (Pinta los datos en el XML)
    // =========================================================================
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

            // CUIDADO: Revisa que estos "get" coinciden con tu clase Incidencia
            holder.tvAsunto.setText(incidencia.getTitulo() != null ? incidencia.getTitulo() : "Sin Asunto");
            holder.tvDescripcion.setText(incidencia.getDescripcion());

            // Asumiendo que guardas el nombre del empleado o la fecha
            // Si no tienes estos datos directos, ajústalos según tu modelo
            holder.tvEmpleadoFecha.setText("Fecha: " + incidencia.getFecha());

            // Lógica de colores para la Píldora de Estado
            String estado = incidencia.getEstado() != null ? incidencia.getEstado().toUpperCase() : "PENDIENTE";
            holder.tvEstado.setText(estado);

            if (estado.equals("RESUELTA") || estado.equals("APROBADA")) {
                holder.tvEstado.setTextColor(Color.parseColor("#10B981")); // Verde
                holder.cardEstado.setCardBackgroundColor(Color.parseColor("#D1FAE5")); // Fondo Verde clarito
            } else {
                holder.tvEstado.setTextColor(Color.parseColor("#EF4444")); // Rojo
                holder.cardEstado.setCardBackgroundColor(Color.parseColor("#FEE2E2")); // Fondo Rojo clarito
            }
        }

        @Override
        public int getItemCount() {
            return lista != null ? lista.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAsunto, tvEmpleadoFecha, tvDescripcion, tvEstado;
            com.google.android.material.card.MaterialCardView cardEstado;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAsunto = itemView.findViewById(R.id.tvAsuntoIncidencia);
                tvEmpleadoFecha = itemView.findViewById(R.id.tvEmpleadoFecha);
                tvDescripcion = itemView.findViewById(R.id.tvDescripcionIncidencia);
                tvEstado = itemView.findViewById(R.id.tvEstadoIncidencia);
                cardEstado = itemView.findViewById(R.id.cardEstado);
            }
        }
    }
}