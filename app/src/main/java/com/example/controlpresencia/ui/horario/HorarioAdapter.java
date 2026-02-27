package com.example.controlpresencia.ui.horario;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.model.DiaHorario;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder> {

    private List<DiaHorario> listaDias;

    public HorarioAdapter(List<DiaHorario> listaDias) {
        this.listaDias = listaDias;
    }

    public void setDias(List<DiaHorario> nuevosDias) {
        this.listaDias = nuevosDias;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder holder, int position) {
        DiaHorario dia = listaDias.get(position);

        // Nombre del día y la inicial (Ej: "Lunes" -> "L")
        holder.tvNombreDia.setText(dia.getNombreDia());
        holder.tvInicialDia.setText(String.valueOf(dia.getNombreDia().charAt(0)).toUpperCase());

        if (dia.isEsLibre()) {
            holder.tvHorasDia.setText("Sin turno");
            holder.tvEstadoTurno.setText("Libre");

            // Colores modo Libre (Gris)
            holder.tvEstadoTurno.setTextColor(Color.parseColor("#64748B"));
            holder.cardEstadoTurno.setCardBackgroundColor(Color.parseColor("#F1F5F9"));
        } else {
            // Recortar los segundos si vienen de la BBDD (Ej: "08:00:00" -> "08:00")
            String entrada = dia.getHoraEntrada() != null && dia.getHoraEntrada().length() >= 5 ? dia.getHoraEntrada().substring(0, 5) : "--:--";
            String salida = dia.getHoraSalida() != null && dia.getHoraSalida().length() >= 5 ? dia.getHoraSalida().substring(0, 5) : "--:--";

            holder.tvHorasDia.setText(entrada + " - " + salida);
            holder.tvEstadoTurno.setText("Turno");

            // Colores modo Turno (Verde)
            holder.tvEstadoTurno.setTextColor(Color.parseColor("#10B981"));
            holder.cardEstadoTurno.setCardBackgroundColor(Color.parseColor("#D1FAE5"));
        }
    }

    @Override
    public int getItemCount() {
        return listaDias != null ? listaDias.size() : 0;
    }

    static class HorarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvInicialDia, tvNombreDia, tvHorasDia, tvEstadoTurno;
        MaterialCardView cardEstadoTurno;

        public HorarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInicialDia = itemView.findViewById(R.id.tvInicialDia);
            tvNombreDia = itemView.findViewById(R.id.tvNombreDia);
            tvHorasDia = itemView.findViewById(R.id.tvHorasDia);
            tvEstadoTurno = itemView.findViewById(R.id.tvEstadoTurno);
            cardEstadoTurno = itemView.findViewById(R.id.cardEstadoTurno);
        }
    }
}