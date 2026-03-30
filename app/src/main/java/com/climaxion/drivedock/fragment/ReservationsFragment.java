package com.climaxion.drivedock.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.activity.MyReservationsActivity;
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


public class ReservationsFragment extends Fragment {

    private RecyclerView rfReservations;
    private ProgressBar progressBar;
    private TextView rfEmpty;
    private View btnViewAll;

    private ReservationAdapter reservationAdapter;
    private List<Reservation> reservationList;
    private ApiInterface apiInterface;
    private SessionManager sessionManager;

    public ReservationsFragment() {
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
        return inflater.inflate(R.layout.fragment_reservations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        apiInterface = ApiClient.getClient();
        sessionManager = new SessionManager(requireContext());
        reservationList = new ArrayList<>();
        reservationAdapter = new ReservationAdapter(requireContext(), reservationList);

        rfReservations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rfReservations.setAdapter(reservationAdapter);

        btnViewAll.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MyReservationsActivity.class));
        });

        loadRecentReservations();
    }

    private void initViews(View view) {
        rfReservations = view.findViewById(R.id.rfReservations);
        progressBar = view.findViewById(R.id.progressBar);
        rfEmpty = view.findViewById(R.id.rfEmpty);
        btnViewAll = view.findViewById(R.id.btnViewAll);
    }

    private void loadRecentReservations() {
        showLoading(true);

        int userId = sessionManager.getUserId();
        Call<List<Reservation>> call = apiInterface.getUserReservations(userId);
        call.enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    reservationList.clear();
                    // Show only last 3 reservations
                    List<Reservation> allReservations = response.body();
                    int count = Math.min(3, allReservations.size());
                    reservationList.addAll(allReservations.subList(0, count));
                    reservationAdapter.notifyDataSetChanged();

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
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rfReservations.setVisibility(show ? View.GONE : View.VISIBLE);
        btnViewAll.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        rfEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rfReservations.setVisibility(show ? View.GONE : View.VISIBLE);
        btnViewAll.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}