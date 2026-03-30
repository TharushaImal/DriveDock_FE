package com.climaxion.drivedock.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.activity.LoginActivity;
import com.climaxion.drivedock.activity.MyReservationsActivity;
import com.climaxion.drivedock.activity.ProfileActivity;
import com.climaxion.drivedock.api.SessionManager;


public class ProfileFragment extends Fragment {

    private TextView pfUserName, pfUserEmail, pfUserPhone, pfMemberSince;
    private Button btnEditProfile, btnMyReservations, btnLogout;

    private SessionManager sessionManager;

    public ProfileFragment() {
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();

        sessionManager = new SessionManager(requireContext());
        loadUserData();
    }

    private void initViews(View view) {
        pfUserName = view.findViewById(R.id.pfUserName);
        pfUserEmail = view.findViewById(R.id.pfUserEmail);
        pfUserPhone = view.findViewById(R.id.pfUserPhone);
        pfMemberSince = view.findViewById(R.id.pfMemberSince);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnMyReservations = view.findViewById(R.id.btnMyReservations);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), ProfileActivity.class));
        });

        btnMyReservations.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), MyReservationsActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserData() {
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();

        pfUserName.setText(userName);
        pfUserEmail.setText(userEmail);
        pfUserPhone.setText("+94 XX XXX XXXX"); // Placeholder
        pfMemberSince.setText("Member since 2024");
    }
}