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
import com.climaxion.drivedock.activity.ServiceBookingActivity;
import com.climaxion.drivedock.adapter.ServiceAdapter;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.model.Service;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ServicesFragment extends Fragment {

    private RecyclerView sfServices;
    private ProgressBar progressBar;
    private TextView sfEmpty;
    private View emptyState;

    private ServiceAdapter serviceAdapter;
    private List<Service> serviceList;
    private ApiInterface apiInterface;

    public ServicesFragment() {
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
        return inflater.inflate(R.layout.fragment_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        apiInterface = ApiClient.getClient();
        serviceList = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(requireContext(), serviceList, service -> {
            Intent intent = new Intent(getActivity(), ServiceBookingActivity.class);
            intent.putExtra("service", service);
            startActivity(intent);
        });

        sfServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        sfServices.setAdapter(serviceAdapter);

        loadServices();
    }

    private void initViews(View view) {
        sfServices = view.findViewById(R.id.sfServices);
        progressBar = view.findViewById(R.id.progressBar);
        sfEmpty = view.findViewById(R.id.sfEmpty);
        emptyState = view.findViewById(R.id.emptyState);
    }

    private void loadServices() {
        showLoading(true);

        Call<List<Service>> call = apiInterface.getServices();
        call.enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    serviceList.clear();
                    serviceList.addAll(response.body());
                    serviceAdapter.notifyDataSetChanged();

                    if (serviceList.isEmpty()) {
                        showEmpty(true);
                    } else {
                        showEmpty(false);
                    }
                } else {
                    showEmpty(true);
                    Toast.makeText(getContext(), "Failed to load services", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                showLoading(false);
                showEmpty(true);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        sfServices.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        if (show){
            emptyState.setVisibility(View.VISIBLE);
            sfServices.setVisibility(View.GONE);
            sfEmpty.setText("No services available at the moment");
        }else{
            emptyState.setVisibility(View.GONE);
            sfServices.setVisibility(View.VISIBLE);
        }
    }
}