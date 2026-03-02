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

// Adaptador para mostrar la lista de fichajes en el historial del trabajador.
public class FichajesAdapter extends RecyclerView.Adapter<FichajesAdapter.FichajeViewHolder> {

    private List<Fichaje> lista = new ArrayList<>();

    // Método para actualizar los datos de la lista y que se refresque la pantalla.
    public void setDatos(List<Fichaje> nuevosDatos) {
        this.lista = nuevosDatos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FichajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Cargamos el diseño de cada fila del historial.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fichaje, parent, false);
        return new FichajeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FichajeViewHolder holder, int position) {
        Fichaje item = lista.get(position);
        
        // Ponemos la fecha, el turno y las horas de entrada/salida.
        holder.tvFecha.setText(item.getFecha() + item.getTurnoTeorico());
        holder.tvHoraEntrada.setText("🟢 " + item.getHoraEntradaFormateada());
        holder.tvHoraSalida.setText("🔴 " + item.getHoraSalidaFormateada());
        holder.tvTotalDia.setText(item.getTotalHoras());

        String textoTotal = item.getTotalHoras();
        holder.tvTotalDia.setText(textoTotal);

        // Cambiamos el color del texto del total según si faltan horas, hay extras o está todo correcto.
        if (textoTotal.contains("⚠️ Faltan")) {
            holder.tvTotalDia.setTextColor(android.graphics.Color.parseColor("#E53935")); // Rojo para avisar.
        } else if (textoTotal.contains("🔥 Extra")) {
            holder.tvTotalDia.setTextColor(android.graphics.Color.parseColor("#FB8C00")); // Naranja para las extras.
        } else if (textoTotal.contains("⏱️")) {
            holder.tvTotalDia.setTextColor(android.graphics.Color.parseColor("#757575")); // Gris si está en curso.
        } else {
            holder.tvTotalDia.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Verde si se ha cumplido.
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    // Clase para enlazar los elementos del XML con el código Java.
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