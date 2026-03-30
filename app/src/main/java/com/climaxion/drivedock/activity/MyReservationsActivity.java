package com.climaxion.drivedock.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.adapter.ReservationAdapter;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.api.SessionManager;
import com.climaxion.drivedock.model.Reservation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReservationsActivity extends AppCompatActivity {

    private RecyclerView mraReservations;
    private ProgressBar progressBar;
    private TextView mraEmpty;

    private ReservationAdapter adapter;
    private List<Reservation> reservationList;
    private ApiInterface apiInterface;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("My Reservations");

        initViews();

        apiInterface = ApiClient.getClient();
        sessionManager = new SessionManager(this);
        reservationList = new ArrayList<>();
        adapter = new ReservationAdapter(this, reservationList);

        mraReservations.setLayoutManager(new LinearLayoutManager(this));
        mraReservations.setAdapter(adapter);

        loadReservations();
    }

    private void initViews() {
        mraReservations = findViewById(R.id.mraReservations);
        progressBar = findViewById(R.id.progressBar);
        mraEmpty = findViewById(R.id.mraEmpty);
    }

    private void loadReservations() {
        showLoading(true);

        int userId = sessionManager.getUserId();
        Call<List<Reservation>> call = apiInterface.getUserReservations(userId);
        call.enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    reservationList.clear();
                    reservationList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (reservationList.isEmpty()) {
                        showEmpty(true);
                    } else {
                        showEmpty(false);
                    }
                } else {
                    showEmpty(true);
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                showLoading(false);
                showEmpty(true);
                Toast.makeText(MyReservationsActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mraReservations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        mraEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        mraReservations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}