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
import com.climaxion.drivedock.api.SessionManager;
import com.climaxion.drivedock.model.User;
import com.climaxion.drivedock.util.ValidationUtils;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText lEmail, lPassword;
    private Button btnLogin;
    private TextView lRegister, lForgotPassword;
    private ProgressBar progressBar;

    private ApiInterface apiInterface;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();

        apiInterface = ApiClient.getClient();
        sessionManager = new SessionManager(this);
    }

    private void initViews() {
        lEmail = findViewById(R.id.lEmail);
        lPassword = findViewById(R.id.lPassword);
        btnLogin = findViewById(R.id.btnLogin);
        lRegister = findViewById(R.id.lRegister);
        lForgotPassword = findViewById(R.id.lForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        lRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
        lForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Please contact support", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        String email = lEmail.getText().toString().trim();
        String password = lPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            lEmail.setError("Email is required");
            lEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            lEmail.setError("Enter a valid email");
            lEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            lPassword.setError("Password is required");
            lPassword.requestFocus();
            return;
        }

        // Show progress
        showProgress(true);

        // Make API call
        Call<JsonObject> call = apiInterface.login(email, password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    if (data.has("success") && data.get("success").getAsBoolean()) {
                        // Create user object from response
                        User user = new User();
                        user.setId(data.get("userId").getAsInt());
                        String userName = data.get("userName").getAsString();
                        if (userName.contains(" ")) {
                            user.setFirstName(userName.substring(0, userName.indexOf(" ")));
                            user.setLastName(userName.substring(userName.indexOf(" ") + 1));
                        } else {
                            user.setFirstName(userName);
                        }
                        user.setEmail(email);

                        // Save session
                        sessionManager.createLoginSession(user);

                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        String error = data.has("error") ? data.get("error").getAsString() : "Login failed";
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        lEmail.setEnabled(!show);
        lPassword.setEnabled(!show);
    }
}