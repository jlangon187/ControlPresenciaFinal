package com.example.controlpresencia.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controlpresencia.R;
import com.example.controlpresencia.data.model.User;
import java.util.List;

// Adaptador para mostrar la lista de empleados en el panel de administrador.
public class AdminEmpleadosAdapter extends RecyclerView.Adapter<AdminEmpleadosAdapter.ViewHolder> {

    private List<User> empleados;
    private final OnItemClickListener listener;

    // Interfaz para gestionar el clic en un empleado de la lista.
    public interface OnItemClickListener {
        void onItemClick(User item);
    }

    public AdminEmpleadosAdapter(List<User> empleados, OnItemClickListener listener) {
        this.empleados = empleados;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el diseño de cada fila de la lista (item_empleado).
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empleado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Ponemos los datos del empleado en los textos correspondientes.
        User empleado = empleados.get(position);
        holder.tvNombre.setText(empleado.getNombreCompleto());
        holder.tvEmail.setText(empleado.getEmail());
        
        // Configuramos el clic para que abra el historial de ese empleado.
        holder.itemView.setOnClickListener(v -> listener.onItemClick(empleado));
    }

    @Override
    public int getItemCount() { return empleados.size(); }

    // Clase interna para guardar las referencias a las vistas de cada fila.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEmail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreEmpleado);
            tvEmail = itemView.findViewById(R.id.tvEmailEmpleado);
        }
    }
}