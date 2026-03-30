package com.climaxion.drivedock.api;

import com.climaxion.drivedock.model.ParkingLocation;
import com.climaxion.drivedock.model.ParkingSlot;
import com.climaxion.drivedock.model.Reservation;
import com.climaxion.drivedock.model.Service;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiInterface {
    // Auth endpoints
    @FormUrlEncoded
    @POST("api/register")
    Call<JsonObject> register(
            @Field("fname") String firstName,
            @Field("lname") String lastName,
            @Field("email") String email,
            @Field("phone_number") String phoneNumber,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("api/login")
    Call<JsonObject> login(
            @Field("email") String email,
            @Field("password") String password
    );

    // Parking endpoints
    @GET("api/parking-locations")
    Call<List<ParkingLocation>> getParkingLocations();

    @GET("api/parking-slots")
    Call<List<ParkingSlot>> getParkingSlots(@Query("location_id") int locationId);

    // Reservation endpoints
    @FormUrlEncoded
    @POST("api/reservations")
    Call<JsonObject> createReservation(
            @Field("user_id") int userId,
            @Field("parking_slot_id") int slotId,
            @Field("start_time") String startTime,
            @Field("end_time") String endTime
    );

    @GET("api/user-reservations")
    Call<List<Reservation>> getUserReservations(@Query("user_id") int userId);

    @FormUrlEncoded
    @POST("api/reservations/complete")
    Call<JsonObject> completeReservation(@Field("reservation_id") int reservationId);

    // Payment endpoints
    @FormUrlEncoded
    @POST("api/payments")
    Call<JsonObject> createPayment(
            @Field("reservation_id") int reservationId,
            @Field("amount") double amount,
            @Field("payment_method") String paymentMethod
    );

    // Service endpoints
    @GET("api/services")
    Call<List<Service>> getServices();

    @FormUrlEncoded
    @POST("api/service-bookings")
    Call<JsonObject> bookService(
            @Field("user_id") int userId,
            @Field("service_id") int serviceId,
            @Field("vehicle_number") String vehicleNumber,
            @Field("booking_date") String bookingDate
    );

    @FormUrlEncoded
    @POST("api/user/update")
    Call<JsonObject> updateProfile(
            @Field("user_id") int userId,
            @Field("fname") String firstName,
            @Field("lname") String lastName,
            @Field("email") String email,
            @Field("phone_number") String phoneNumber
    );
}
