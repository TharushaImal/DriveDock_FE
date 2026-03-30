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
import com.climaxion.drivedock.model.ParkingSlot;

import java.util.List;

public class ParkingSlotAdapter extends RecyclerView.Adapter<ParkingSlotAdapter.ViewHolder> {

    private Context context;
    private List<ParkingSlot> slots;
    private OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onSlotClick(ParkingSlot slot);
    }

    public ParkingSlotAdapter(Context context, List<ParkingSlot> slots, OnSlotClickListener listener) {
        this.context = context;
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParkingSlotAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingSlotAdapter.ViewHolder holder, int position) {

        ParkingSlot slot = slots.get(position);

        holder.tvSlotNumber.setText(slot.getSlotNumber());
        holder.tvPrice.setText(String.format("LKR %.2f/hour", slot.getPricePerHour()));

        // Set status styling
        if (slot.isAvailable()) {
            holder.tvStatus.setText("Available");
            holder.tvStatus.setTextColor(context.getColor(R.color.success));
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.md_theme_background_highContrast));
            holder.cardView.setEnabled(true);
        } else {
            holder.tvStatus.setText(slot.getStatus());
            holder.tvStatus.setTextColor(context.getColor(R.color.md_theme_error_highContrast));
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.md_theme_onSurfaceVariant));
            holder.cardView.setEnabled(false);
        }

        holder.cardView.setOnClickListener(v -> {
            if (slot.isAvailable()) {
                listener.onSlotClick(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvSlotNumber, tvPrice, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvSlotNumber = itemView.findViewById(R.id.tvSlotNumber);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }

}
