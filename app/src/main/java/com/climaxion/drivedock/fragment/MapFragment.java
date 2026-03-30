package com.climaxion.drivedock.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.activity.ParkingMapActivity;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.model.ParkingLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ApiInterface apiInterface;
    private Map<Marker, ParkingLocation> markerLocationMap;
    private LatLng currentLocation;

    public MapFragment() {
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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiInterface = ApiClient.getClient();
        markerLocationMap = new HashMap<>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set click listener for floating action button
        view.findViewById(R.id.mfbMyLocation).setOnClickListener(v -> {
            if (currentLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
            } else {
                getCurrentLocation();
            }
        });

        View listBtn = view.findViewById(R.id.mfbList);
        if (listBtn != null) {
            listBtn.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), ParkingMapActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Set marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            ParkingLocation location = markerLocationMap.get(marker);
            if (location != null) {
                showParkingDetails(location);
            }
            return false;
        });

        // Get current location
        getCurrentLocation();

        // Load parking locations
        loadParkingLocations();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f));
                        mMap.setMyLocationEnabled(true);
                    } else {
                        // Default location (Colombo)
                        currentLocation = new LatLng(6.9271, 79.8612);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12f));
                    }
                });
    }

    private void loadParkingLocations() {
        Call<List<ParkingLocation>> call = apiInterface.getParkingLocations();
        call.enqueue(new Callback<List<ParkingLocation>>() {
            @Override
            public void onResponse(Call<List<ParkingLocation>> call, Response<List<ParkingLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ParkingLocation location : response.body()) {
                        addMarkerForLocation(location);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ParkingLocation>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load parking locations: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarkerForLocation(ParkingLocation location) {
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        // Set marker color based on availability
        float markerColor;
        if (location.getAvailableSlots() > 0) {
            markerColor = BitmapDescriptorFactory.HUE_GREEN;
        } else if (location.getAvailableSlots() == 0) {
            markerColor = BitmapDescriptorFactory.HUE_RED;
        } else {
            markerColor = BitmapDescriptorFactory.HUE_ORANGE;
        }

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(location.getName())
                .snippet(location.getAvailableSlots() + " slots available")
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

        if (marker != null) {
            markerLocationMap.put(marker, location);
        }
    }

    private void showParkingDetails(ParkingLocation location) {
        // Show a custom info window with more details
        String message = String.format("%s\nAddress: %s\nAvailable Slots: %d/%d\nPrice: LKR %.2f/hour",
                location.getName(),
                location.getAddress() != null ? location.getAddress() : "N/A",
                location.getAvailableSlots(),
                location.getTotalSlots(),
                location.getSlots() != null && !location.getSlots().isEmpty()
                        ? location.getSlots().get(0).getPricePerHour() : 0);

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied. Using default location.",
                        Toast.LENGTH_SHORT).show();
                currentLocation = new LatLng(6.9271, 79.8612);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12f));
            }
        }
    }
}