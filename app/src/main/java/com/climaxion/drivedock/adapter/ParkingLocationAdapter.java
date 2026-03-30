package com.climaxion.drivedock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.model.ParkingLocation;

import java.util.List;

public class ParkingLocationAdapter extends RecyclerView.Adapter<ParkingLocationAdapter.ViewHolder> {
    private Context context;
    private List<ParkingLocation> locations;
    private OnLocationClickListener listener;

    public ParkingLocationAdapter(Context context, List<ParkingLocation> locations, OnLocationClickListener listener) {
        this.context = context;
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParkingLocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingLocationAdapter.ViewHolder holder, int position) {

        ParkingLocation location = locations.get(position);

        holder.tvName.setText(location.getName());
        holder.tvAddress.setText(location.getAddress() != null ? location.getAddress() : "Address not available");
        holder.tvSlots.setText(String.format("%d/%d slots available",
                location.getAvailableSlots(), location.getTotalSlots()));

        // Set price
        if (location.getSlots() != null && !location.getSlots().isEmpty()) {
            holder.tvPrice.setText(String.format("LKR %.2f/hour",
                    location.getSlots().get(0).getPricePerHour()));
        } else {
            holder.tvPrice.setText("Price not available");
        }

        holder.itemView.setOnClickListener(v -> listener.onLocationClick(location));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public interface OnLocationClickListener {
        void onLocationClick(ParkingLocation location);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvSlots, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvSlots = itemView.findViewById(R.id.tvSlots);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }

}
