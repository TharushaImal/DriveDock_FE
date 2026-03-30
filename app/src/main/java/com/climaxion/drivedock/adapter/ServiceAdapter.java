package com.climaxion.drivedock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.model.Service;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder>{

    private Context context;
    private List<Service> services;
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public ServiceAdapter(Context context, List<Service> services, OnServiceClickListener listener) {
        this.context = context;
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceAdapter.ViewHolder holder, int position) {

        Service service = services.get(position);

        holder.tvName.setText(service.getName());
        holder.tvDescription.setText(service.getDescription() != null ? service.getDescription() : "");
        holder.tvPrice.setText(String.format("LKR %.2f", service.getPrice()));

        holder.cardView.setOnClickListener(v -> listener.onServiceClick(service));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView tvName, tvDescription, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
