package com.climaxion.drivedock.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingLocation implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("opening_time")
    private String openingTime;

    @SerializedName("closing_time")
    private String closingTime;

    @SerializedName("total_slots")
    private int totalSlots;

    @SerializedName("available_slots")
    private int availableSlots;

    @SerializedName("slots")
    private List<ParkingSlot> slots;

    private transient float distanceFromUser;

}
