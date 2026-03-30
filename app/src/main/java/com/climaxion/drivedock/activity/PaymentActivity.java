package com.climaxion.drivedock.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.api.SessionManager;
import com.google.gson.JsonObject;

import java.io.IOException;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private static final int PAYHERE_REQUEST_CODE = 1001;

    private TextView paReservationId, paAmount, paStatus;
    private Button btnPay, btnCancel;
    private ProgressBar progressBar;
    private RadioGroup paPaymentMethod;
    private CardView cardStatus;
    private ImageView paStatusIcon;

    private int reservationId;
    private double amount;
    private ApiInterface apiInterface;
    private SessionManager sessionManager;
    private String selectedMethod = "CARD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Payment");

        initViews();
        setupClickListeners();

        apiInterface = ApiClient.getClient();
        sessionManager = new SessionManager(this);

        // For demo purposes, get reservation ID from intent or use a placeholder
        reservationId = getIntent().getIntExtra("reservation_id", 1);
        amount = getIntent().getDoubleExtra("amount", 500.00);

        displayPaymentDetails();

        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL); // Use SANDBOX_URL for testing
    }

    private void initViews() {
        paReservationId = findViewById(R.id.paReservationId);
        paAmount = findViewById(R.id.paAmount);
        paStatus = findViewById(R.id.paStatus);
        btnPay = findViewById(R.id.btnPay);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
        paPaymentMethod = findViewById(R.id.paPaymentMethod);
        cardStatus = findViewById(R.id.cardStatus);
        paStatusIcon = findViewById(R.id.paStatusIcon);
    }

    private void setupClickListeners() {
        btnPay.setOnClickListener(v -> initiatePayHerePayment());
        btnCancel.setOnClickListener(v -> finish());

        paPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.paCreditCard) {
                selectedMethod = "CARD";
            } else if (checkedId == R.id.paMobileMoney) {
                selectedMethod = "MOBILE";
            } else if (checkedId == R.id.paInternetBanking) {
                selectedMethod = "BANKING";
            }
        });
    }

    private void displayPaymentDetails() {
        paReservationId.setText("Reservation #" + reservationId);
        paAmount.setText(String.format("LKR %.2f", amount));
    }

    private void initiatePayHerePayment() {
        showProgress(true);

        // Create PayHere payment request
        InitRequest req = new InitRequest();
        req.setSandBox(true);

        // Merchant ID (Get from PayHere dashboard)
        req.setMerchantId("1225094"); // Replace with your merchant ID
        req.setMerchantSecret("OTUwNTE2MTM4MjY3MzE5Njg3MDM4Njc2NzMxMTMxNTQ4NDcxMTI2");

        // Amount and currency
        req.setCurrency("LKR");
        req.setAmount(amount);

        // Order/Reservation ID (must be unique)
        String orderId = "RES_" + reservationId + "_" + System.currentTimeMillis();
        req.setOrderId(orderId);
        req.setItemsDescription("");

        // Customer details
        req.getCustomer().setFirstName(sessionManager.getUserName().split(" ")[0]);
        String lastName = sessionManager.getUserName().split(" ").length > 1
                ? sessionManager.getUserName().split(" ")[1] : "User";
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(sessionManager.getUserEmail());
        req.getCustomer().setPhone("0771234567"); // Get from user profile
        req.getCustomer().getAddress().setAddress("No 1, Colombo Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // Items
        req.getItems().add(new lk.payhere.androidsdk.model.Item(null, "Parking Reservation", 1, amount));

        // Custom parameters
        req.setCustom1(String.valueOf(reservationId));
        req.setCustom2("Parking");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

        payhereLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> payhereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                showProgress(false);
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        PHResponse<StatusResponse> response =
                                (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                        if (response != null && response.isSuccess()) {
                            StatusResponse statusResponse = response.getData();
                            String orderId = ""; // Order ID not returned in StatusResponse in this SDK version
                            String paymentId = String.valueOf(statusResponse.getPaymentNo());

                            Log.i("PAYHERE", "Payment Success! Payment No: " + paymentId);

                            confirmPaymentToBackend(orderId, paymentId);
                            updatePaymentStatus(true, "Payment Successful! Your parking is confirmed.");

                        } else if (response != null) {
                            Log.e("PAYHERE", "Payment failed status: " + response.getStatus());
                            updatePaymentStatus(false, "Payment failed: " + response.toString());
                        } else {
                            updatePaymentStatus(false, "Payment failed. Please try again.");
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.e("PAYHERE", "Payment Canceled!");
                    updatePaymentStatus(false, "Payment was cancelled");
                } else {
                    updatePaymentStatus(false, "Payment failed");
                }

            });

    private void confirmPaymentToBackend(String orderId, String paymentId) {
        // Call backend API to confirm payment
        int userId = sessionManager.getUserId();
        Call<ResponseBody> call = apiInterface.createPayment(userId, reservationId, amount, selectedMethod);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String result = response.body().string();
                        Log.d("PaymentActivity", "Backend response: " + result);
                        Toast.makeText(PaymentActivity.this, "Payment confirmed by backend", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("PaymentActivity", "Backend confirmation failed", t);
            }
        });
    }

    private void updatePaymentStatus(boolean success, String message) {
        cardStatus.setVisibility(View.VISIBLE);
        paStatus.setText(message);

        if (success) {
            paStatusIcon.setImageResource(R.drawable.check_circle_24px);
            paStatus.setTextColor(getColor(R.color.success));
            btnPay.setEnabled(false);
            btnPay.setText("PAID");

            // Finish activity after delay
            new android.os.Handler().postDelayed(this::finish, 3000);
        } else {
            paStatusIcon.setImageResource(R.drawable.error_24px);
            paStatus.setTextColor(getColor(R.color.md_theme_error));
            btnPay.setEnabled(true);
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPay.setEnabled(!show);
        btnCancel.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
