package com.climaxion.drivedock.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.activity.MyReservationsActivity;
import com.climaxion.drivedock.activity.ParkingMapActivity;
import com.climaxion.drivedock.activity.ServiceBookingActivity;
import com.climaxion.drivedock.adapter.ParkingLocationAdapter;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.api.SessionManager;
import com.climaxion.drivedock.model.ParkingLocation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {

    private TextView hfWelcome, hfNearbyTitle;
    private CardView hfFindParking, hfBookService, hfMyBookings, hfEmergency;
    private RecyclerView hfNearbyParking;
    private ParkingLocationAdapter locationAdapter;
    private List<ParkingLocation> locationList;

    private SessionManager sessionManager;
    private ApiInterface apiInterface;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();

        sessionManager = new SessionManager(requireContext());
        apiInterface = ApiClient.getClient();

        // Set welcome message
        String userName = sessionManager.getUserName();
        hfWelcome.setText("Welcome back,\n" + userName + "!");

        loadNearbyParking();
    }

    private void initViews(View view) {
        hfWelcome = view.findViewById(R.id.hfWelcome);
        hfNearbyTitle = view.findViewById(R.id.hfNearbyTitle);
        hfFindParking = view.findViewById(R.id.hfFindParking);
        hfBookService = view.findViewById(R.id.hfBookService);
        hfMyBookings = view.findViewById(R.id.hfMyBookings);
        hfEmergency = view.findViewById(R.id.hfEmergency);
        hfNearbyParking = view.findViewById(R.id.hfNearbyParking);

        // Setup RecyclerView
        locationList = new ArrayList<>();
        locationAdapter = new ParkingLocationAdapter(requireContext(), locationList,
                location -> {
                    // Navigate to parking map with selected location
                    Intent intent = new Intent(getActivity(), ParkingMapActivity.class);
                    intent.putExtra("location_id", location.getId());
                    intent.putExtra("location_name", location.getName());
                    startActivity(intent);
                });
        hfNearbyParking.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        hfNearbyParking.setAdapter(locationAdapter);
    }

    private void setupClickListeners() {
        hfFindParking.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ParkingMapActivity.class));
        });

        hfBookService.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ServiceBookingActivity.class));
        });

        hfMyBookings.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MyReservationsActivity.class));
        });

        hfEmergency.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Emergency services will be available soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadNearbyParking() {
        // Show loading state
        hfNearbyTitle.setText("Loading nearby parking...");

        Call<List<ParkingLocation>> call = apiInterface.getParkingLocations();
        call.enqueue(new Callback<List<ParkingLocation>>() {
            @Override
            public void onResponse(Call<List<ParkingLocation>> call, Response<List<ParkingLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    locationList.clear();
                    // Limit to first 5 for home screen
                    List<ParkingLocation> allLocations = response.body();
                    int count = Math.min(5, allLocations.size());
                    locationList.addAll(allLocations.subList(0, count));
                    locationAdapter.notifyDataSetChanged();

                    hfNearbyTitle.setText("Nearby Parking Spots");

                    if (locationList.isEmpty()) {
                        hfNearbyTitle.setText("No parking spots available nearby");
                    }
                } else {
                    hfNearbyTitle.setText("Failed to load parking spots");
                }
            }

            @Override
            public void onFailure(Call<List<ParkingLocation>> call, Throwable t) {
                hfNearbyTitle.setText("Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to load parking: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}