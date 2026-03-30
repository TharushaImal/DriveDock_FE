package com.climaxion.drivedock.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.adapter.ParkingSlotAdapter;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.model.ParkingLocation;
import com.climaxion.drivedock.model.ParkingSlot;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ParkingSlot selectedSlot;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ApiInterface apiInterface;

    private View bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView tvLocationName, tvLocationAddress, tvAvailableSlots;
    private RecyclerView rvSlots;
    private ProgressBar progressBar;
    private Button btnReserve;
    private FloatingActionButton fabMyLocation;

    private ParkingSlotAdapter slotAdapter;
    private List<ParkingSlot> slotList;
    private ParkingLocation currentLocation;
    private Map<Marker, ParkingLocation> markerLocationMap;
    private LatLng currentUserLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Select Parking Spot");

        initViews();
        setupBottomSheet();

        apiInterface = ApiClient.getClient();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        markerLocationMap = new HashMap<>();
        slotList = new ArrayList<>();
        slotAdapter = new ParkingSlotAdapter(this, slotList, slot -> {
            selectedSlot = slot;
            Intent intent = new Intent(ParkingMapActivity.this, ReservationActivity.class);
            intent.putExtra("location", currentLocation);
            intent.putExtra("slot", slot);
            startActivity(intent);
        });

        rvSlots.setLayoutManager(new LinearLayoutManager(this));
        rvSlots.setAdapter(slotAdapter);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fabMyLocation.setOnClickListener(v -> {
            if (currentUserLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f));
            } else {
                getCurrentLocation();
            }
        });

        loadParkingLocations();
    }

    private void initViews() {
        bottomSheet = findViewById(R.id.bottomSheet);
        tvLocationName = findViewById(R.id.tvLocationName);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        tvAvailableSlots = findViewById(R.id.tvAvailableSlots);
        rvSlots = findViewById(R.id.rvSlots);
        progressBar = findViewById(R.id.progressBar);
        btnReserve = findViewById(R.id.btnReserve);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        btnReserve.setOnClickListener(v -> {
            if (currentLocation != null && selectedSlot != null) {
                Intent intent = new Intent(ParkingMapActivity.this, ReservationActivity.class);
                intent.putExtra("location", currentLocation);
                intent.putExtra("slot", selectedSlot);
                startActivity(intent);
            } else if (selectedSlot == null) {
                Toast.makeText(this, "Please select a parking slot first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    btnReserve.setVisibility(View.GONE);
                } else {
                    btnReserve.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMarkerClickListener(marker -> {
            ParkingLocation location = markerLocationMap.get(marker);
            if (location != null) {
                showLocationDetails(location);
            }
            return false;
        });

        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 14f));
                        mMap.setMyLocationEnabled(true);
                    } else {
                        // Default location (Colombo)
                        currentUserLocation = new LatLng(6.9271, 79.8612);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
                    }
                });
    }

    private void loadParkingLocations() {
        progressBar.setVisibility(View.VISIBLE);

        Call<List<ParkingLocation>> call = apiInterface.getParkingLocations();
        call.enqueue(new Callback<List<ParkingLocation>>() {
            @Override
            public void onResponse(Call<List<ParkingLocation>> call, Response<List<ParkingLocation>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    for (ParkingLocation location : response.body()) {
                        addMarkerForLocation(location);
                    }
                } else {
                    Toast.makeText(ParkingMapActivity.this, "Failed to load parking locations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ParkingLocation>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ParkingMapActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarkerForLocation(ParkingLocation location) {
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        float markerColor = location.getAvailableSlots() > 0
                ? BitmapDescriptorFactory.HUE_GREEN
                : BitmapDescriptorFactory.HUE_RED;

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(location.getName())
                .snippet(location.getAvailableSlots() + " slots available")
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

        if (marker != null) {
            markerLocationMap.put(marker, location);
        }
    }

    private void showLocationDetails(ParkingLocation location) {
        currentLocation = location;

        tvLocationName.setText(location.getName());
        tvLocationAddress.setText(location.getAddress() != null ? location.getAddress() : "Address not available");
        tvAvailableSlots.setText(String.format("%d / %d slots available",
                location.getAvailableSlots(), location.getTotalSlots()));

        loadSlotsForLocation(location.getId());

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Move camera to selected location
        LatLng locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 16f));
    }

    private void loadSlotsForLocation(int locationId) {
        slotList.clear();
        slotAdapter.notifyDataSetChanged();

        Call<List<ParkingSlot>> call = apiInterface.getParkingSlots(locationId);
        call.enqueue(new Callback<List<ParkingSlot>>() {
            @Override
            public void onResponse(Call<List<ParkingSlot>> call, Response<List<ParkingSlot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    slotList.addAll(response.body());
                    slotAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ParkingSlot>> call, Throwable t) {
                Toast.makeText(ParkingMapActivity.this, "Failed to load slots", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}