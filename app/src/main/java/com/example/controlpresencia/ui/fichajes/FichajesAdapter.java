package com.example.controlpresencia.ui.fichajes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.model.Fichaje;
import java.util.ArrayList;
import java.util.List;

public class FichajesAdapter extends RecyclerView.Adapter<FichajesAdapter.FichajeViewHolder> {

    private List<Fichaje> lista = new ArrayList<>();

    public void setDatos(List<Fichaje> nuevosDatos) {
        this.lista = nuevosDatos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FichajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fichaje, parent, false);
        return new FichajeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FichajeViewHolder holder, int position) {
        Fichaje item = lista.get(position);
        holder.tvFecha.setText(item.getFecha() + item.getTurnoTeorico());
        holder.tvHoraEntrada.setText("🟢 " + item.getHoraEntradaFormateada());
        holder.tvHoraSalida.setText("🔴 " + item.getHoraSalidaFormateada());
        holder.tvTotalDia.setText(item.getTotalHoras());

        String textoTotal = item.getTotalHoras();
        holder.tvTotalDia.setText(textoTotal);

        if (textoTotal.contains("⚠️ Faltan")) {
            holder.tvTotalDia.setTextColor(android.graphics.Color.parseColor("#E53935")); // Rojo
        } else if (textoTotal.contains("🔥 Extra")) {
            holder.tvTotalDia.setTextColor(android.graphics.Color.parseColor("#FB8C00")); // Naranja
        } else if (textoTotal.contains("⏱️")) {
            holder.tvTotalDia.setTextColor(android.graphics.Color.parseColor("#757575")); // Gris
        } else {
            holder.tvTotalDia.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Verde
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class FichajeViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvHoraEntrada, tvHoraSalida, tvTotalDia;

        public FichajeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHoraEntrada = itemView.findViewById(R.id.tvHoraEntrada);
            tvHoraSalida = itemView.findViewById(R.id.tvHoraSalida);
            tvTotalDia = itemView.findViewById(R.id.tvTotalDia);
        }
    }
}