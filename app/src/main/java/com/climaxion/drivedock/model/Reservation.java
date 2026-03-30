package com.climaxion.drivedock.model;


import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @SerializedName("id")
    private int id;

    @SerializedName("slot_number")
    private String slotNumber;

    @SerializedName("location_name")
    private String locationName;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("status")
    private String status;

    @SerializedName("total_amount")
    private double totalAmount;

    public boolean isConfirmed() {
        return "CONFIRMED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
}
