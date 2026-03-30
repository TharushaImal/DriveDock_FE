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
import com.climaxion.drivedock.model.Reservation;
import com.climaxion.drivedock.util.DateUtils;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private Context context;
    private List<Reservation> reservations;

    public ReservationAdapter(Context context, List<Reservation> reservations) {
        this.context = context;
        this.reservations = reservations;
    }

    @NonNull
    @Override
    public ReservationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationAdapter.ViewHolder holder, int position) {

        Reservation reservation = reservations.get(position);

        holder.tvLocation.setText(reservation.getLocationName());
        holder.tvSlot.setText("Slot: " + reservation.getSlotNumber());
        holder.tvDateTime.setText(String.format("%s - %s",
                DateUtils.formatForDisplay(reservation.getStartTime()),
                DateUtils.formatForDisplay(reservation.getEndTime())));

        // Set status with appropriate color
        String status = reservation.getStatus();
        holder.tvStatus.setText(status);

        int statusColor;
        if (reservation.isConfirmed()) {
            statusColor = context.getColor(R.color.success1);
        } else if (reservation.isPending()) {
            statusColor = context.getColor(R.color.md_theme_error);
        } else if (reservation.isCompleted()) {
            statusColor = context.getColor(R.color.md_theme_primary);
        } else {
            statusColor = context.getColor(R.color.md_theme_error_highContrast);
        }
        holder.tvStatus.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation, tvSlot, tvDateTime, tvStatus;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSlot = itemView.findViewById(R.id.tvSlot);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
