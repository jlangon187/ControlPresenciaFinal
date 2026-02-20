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

public class AdminHistorialAdapter extends RecyclerView.Adapter<AdminHistorialAdapter.ViewHolder> {

    private List<Fichaje> lista;
    private final OnFichajeClickListener listener;

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fichaje, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Fichaje item = lista.get(position);

        holder.tvFecha.setText(item.getFecha());
        holder.tvHoraEntrada.setText("🟢 " + item.getHoraEntradaFormateada());
        holder.tvHoraSalida.setText("🔴 " + item.getHoraSalidaFormateada());
        holder.tvTotalDia.setText(item.getTotalHoras());

        // Al hacer clic, enviamos el evento
        holder.itemView.setOnClickListener(v -> listener.onFichajeClick(item));
    }

    @Override
    public int getItemCount() { return lista.size(); }

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