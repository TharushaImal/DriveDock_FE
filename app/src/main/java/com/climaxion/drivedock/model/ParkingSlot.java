package com.climaxion.drivedock.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingSlot implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("slot_number")
    private String slotNumber;

    @SerializedName("status")
    private String status;

    @SerializedName("price_per_hour")
    private double pricePerHour;

    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }
}
