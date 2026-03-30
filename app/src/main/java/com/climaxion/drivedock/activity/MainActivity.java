package com.climaxion.drivedock.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.api.SessionManager;
import com.climaxion.drivedock.fragment.HomeFragment;
import com.climaxion.drivedock.fragment.MapFragment;
import com.climaxion.drivedock.fragment.ProfileFragment;
import com.climaxion.drivedock.fragment.ReservationsFragment;
import com.climaxion.drivedock.fragment.ServicesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupBottomNavigation();

        // Load default fragment
        loadFragment(new HomeFragment());
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_map) {
                selectedFragment = new MapFragment();
            } else if (itemId == R.id.nav_services) {
                selectedFragment = new ServicesFragment();
            } else if (itemId == R.id.nav_reservations) {
                selectedFragment = new ReservationsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }
}