package com.climaxion.drivedock.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.api.SessionManager;
import com.climaxion.drivedock.model.User;
import com.climaxion.drivedock.util.ValidationUtils;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPhone;
    private Button btnUpdate, btnCancel;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private ApiInterface apiInterface;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Edit Profile");

        initViews();
        setupClickListeners();

        sessionManager = new SessionManager(this);
        apiInterface = ApiClient.getClient();

        loadUserData();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnUpdate.setOnClickListener(v -> updateProfile());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        currentUser = sessionManager.getUserDetails();

        if (currentUser != null) {
            etFirstName.setText(currentUser.getFirstName());
            etLastName.setText(currentUser.getLastName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhoneNumber());
        }
    }

    private void updateProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validation
        if (!ValidationUtils.isValidName(firstName)) {
            etFirstName.setError("First name must be at least 2 characters");
            etFirstName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidName(lastName)) {
            etLastName.setError("Last name must be at least 2 characters");
            etLastName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPhone(phone)) {
            etPhone.setError("Enter a valid Sri Lankan phone number");
            etPhone.requestFocus();
            return;
        }

        showProgress(true);

        // Call API to update profile
        // Note: You'll need to add this endpoint to your backend
        Call<JsonObject> call = apiInterface.updateProfile(
                sessionManager.getUserId(),
                firstName,
                lastName,
                email,
                phone
        );

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    if (data.has("message")) {
                        // Update session with new data
                        currentUser.setFirstName(firstName);
                        currentUser.setLastName(lastName);
                        currentUser.setEmail(email);
                        currentUser.setPhoneNumber(phone);
                        sessionManager.createLoginSession(currentUser);

                        Toast.makeText(ProfileActivity.this,
                                data.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (data.has("error")) {
                        Toast.makeText(ProfileActivity.this,
                                data.get("error").getAsString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!show);
        btnCancel.setEnabled(!show);

        etFirstName.setEnabled(!show);
        etLastName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPhone.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}