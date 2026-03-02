package com.example.controlpresencia.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.model.Fichaje;
import java.util.List;

// Adaptador para mostrar el historial de fichajes de un empleado en la vista del administrador.
public class AdminHistorialAdapter extends RecyclerView.Adapter<AdminHistorialAdapter.ViewHolder> {

    private List<Fichaje> lista;
    private final OnFichajeClickListener listener;

    // Interfaz para saber cuándo se ha pinchado en un fichaje concreto.
    public interface OnFichajeClickListener {
        void onFichajeClick(Fichaje fichaje);
    }

    public AdminHistorialAdapter(List<Fichaje> lista, OnFichajeClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el diseño de la fila para cada fichaje (item_fichaje).
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fichaje, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Fichaje item = lista.get(position);

        // Rellenamos la información del fichaje: fecha, turno, entrada, salida y total de horas.
        holder.tvFecha.setText(item.getFecha() + item.getTurnoTeorico());
        holder.tvHoraEntrada.setText("🟢 " + item.getHoraEntradaFormateada());
        holder.tvHoraSalida.setText("🔴 " + item.getHoraSalidaFormateada());
        holder.tvTotalDia.setText(item.getTotalHoras());

        // Al pulsar en el fichaje, ejecutamos el listener (normalmente para ver el mapa).
        holder.itemView.setOnClickListener(v -> listener.onFichajeClick(item));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    // Clase para guardar las vistas de cada elemento de la lista.
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHoraEntrada, tvHoraSalida, tvTotalDia;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHoraEntrada = itemView.findViewById(R.id.tvHoraEntrada);
            tvHoraSalida = itemView.findViewById(R.id.tvHoraSalida);
            tvTotalDia = itemView.findViewById(R.id.tvTotalDia);
        }
    }
}