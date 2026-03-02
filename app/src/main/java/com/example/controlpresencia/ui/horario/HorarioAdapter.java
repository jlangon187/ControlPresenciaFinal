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

// Adaptador para mostrar el horario semanal del trabajador.
public class HorarioAdapter extends RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder> {

    private List<DiaHorario> listaDias;

    public HorarioAdapter(List<DiaHorario> listaDias) {
        this.listaDias = listaDias;
    }

    // Actualiza la lista de días y refresca la vista.
    public void setDias(List<DiaHorario> nuevosDias) {
        this.listaDias = nuevosDias;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el diseño de cada fila del horario (item_horario).
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horario, parent, false);
        return new HorarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder holder, int position) {
        DiaHorario dia = listaDias.get(position);

        // Ponemos el nombre del día y su inicial.
        holder.tvNombreDia.setText(dia.getNombreDia());
        holder.tvInicialDia.setText(String.valueOf(dia.getNombreDia().charAt(0)).toUpperCase());

        // Si es día libre, lo marcamos en gris y ponemos "Sin turno".
        if (dia.isEsLibre()) {
            holder.tvHorasDia.setText("Sin turno");
            holder.tvEstadoTurno.setText("Libre");

            holder.tvEstadoTurno.setTextColor(Color.parseColor("#64748B"));
            holder.cardEstadoTurno.setCardBackgroundColor(Color.parseColor("#F1F5F9"));
        } else {
            // Si hay turno, mostramos las horas de entrada y salida en verde.
            String entrada = dia.getHoraEntrada() != null && dia.getHoraEntrada().length() >= 5 ? dia.getHoraEntrada().substring(0, 5) : "--:--";
            String salida = dia.getHoraSalida() != null && dia.getHoraSalida().length() >= 5 ? dia.getHoraSalida().substring(0, 5) : "--:--";

            holder.tvHorasDia.setText(entrada + " - " + salida);
            holder.tvEstadoTurno.setText("Turno");

            holder.tvEstadoTurno.setTextColor(Color.parseColor("#10B981"));
            holder.cardEstadoTurno.setCardBackgroundColor(Color.parseColor("#D1FAE5"));
        }
    }

    @Override
    public int getItemCount() {
        return listaDias != null ? listaDias.size() : 0;
    }

    // Clase para guardar las referencias a las vistas de cada fila.
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