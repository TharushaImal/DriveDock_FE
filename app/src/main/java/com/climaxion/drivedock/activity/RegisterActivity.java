package com.climaxion.drivedock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.util.ValidationUtils;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText rFirstName, rLastName, rEmail, rPhone, rPassword, rConfirmPassword;
    private Button btnRegister;
    private TextView rLogin;
    private ProgressBar progressBar;

    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupClickListeners();

        apiInterface = ApiClient.getClient();
    }

    private void initViews() {
        rFirstName = findViewById(R.id.rFirstName);
        rLastName = findViewById(R.id.rLastName);
        rEmail = findViewById(R.id.rEmail);
        rPhone = findViewById(R.id.rPhone);
        rPassword = findViewById(R.id.rPassword);
        rConfirmPassword = findViewById(R.id.rConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        rLogin = findViewById(R.id.rLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        rLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String firstName = rFirstName.getText().toString().trim();
        String lastName = rLastName.getText().toString().trim();
        String email = rEmail.getText().toString().trim();
        String phone = rPhone.getText().toString().trim();
        String password = rPassword.getText().toString().trim();
        String confirmPassword = rConfirmPassword.getText().toString().trim();

        // Validation
        if (!ValidationUtils.isValidName(firstName)) {
            rFirstName.setError("First name must be at least 2 characters");
            rFirstName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidName(lastName)) {
            rLastName.setError("Last name must be at least 2 characters");
            rLastName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            rEmail.setError("Enter a valid email");
            rEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPhone(phone)) {
            rPhone.setError("Enter a valid Sri Lankan phone number");
            rPhone.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            rPassword.setError("Password must be at least 8 characters with letters and numbers");
            rPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            rConfirmPassword.setError("Passwords do not match");
            rConfirmPassword.requestFocus();
            return;
        }

        // Show progress
        showProgress(true);

        // Make API call
        Call<JsonObject> call = apiInterface.register(firstName, lastName, email, phone, password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    if (data.has("message")) {
                        Toast.makeText(RegisterActivity.this,
                                data.get("message").getAsString(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else if (data.has("error")) {
                        Toast.makeText(RegisterActivity.this,
                                data.get("error").getAsString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress(false);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);

        rFirstName.setEnabled(!show);
        rLastName.setEnabled(!show);
        rEmail.setEnabled(!show);
        rPhone.setEnabled(!show);
        rPassword.setEnabled(!show);
        rConfirmPassword.setEnabled(!show);
    }
}