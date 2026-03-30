package com.climaxion.drivedock.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.api.SessionManager;
import com.climaxion.drivedock.model.ParkingLocation;
import com.climaxion.drivedock.model.ParkingSlot;
import com.climaxion.drivedock.util.DateUtils;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationActivity extends AppCompatActivity {

    private TextView tvLocationName, tvSlotNumber, tvPricePerHour, tvTotalPrice;
    private EditText etStartDate, etStartTime, etEndDate, etEndTime;
    private Button btnConfirmReservation;
    private ProgressBar progressBar;

    private ParkingLocation location;
    private ParkingSlot slot;
    private SessionManager sessionManager;
    private ApiInterface apiInterface;

    private Calendar startCalendar;
    private Calendar endCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reservation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Confirm Reservation");

        // Get data from intent
        location = (ParkingLocation) getIntent().getSerializableExtra("location");
        slot = (ParkingSlot) getIntent().getSerializableExtra("slot");

        if (location == null || slot == null) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupDatePickers();
        setupClickListeners();

        sessionManager = new SessionManager(this);
        apiInterface = ApiClient.getClient();

        // Set initial values
        tvLocationName.setText(location.getName());
        tvSlotNumber.setText("Slot: " + slot.getSlotNumber());
        tvPricePerHour.setText(String.format("LKR %.2f/hour", slot.getPricePerHour()));

        // Set default times (current time and 1 hour later)
        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);

        endCalendar = (Calendar) startCalendar.clone();
        endCalendar.add(Calendar.HOUR_OF_DAY, 1);

        updateDateTimeDisplays();
        calculateTotalPrice();
    }

    private void initViews() {
        tvLocationName = findViewById(R.id.tvLocationName);
        tvSlotNumber = findViewById(R.id.tvSlotNumber);
        tvPricePerHour = findViewById(R.id.tvPricePerHour);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        etStartDate = findViewById(R.id.etStartDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndDate = findViewById(R.id.etEndDate);
        etEndTime = findViewById(R.id.etEndTime);
        btnConfirmReservation = findViewById(R.id.btnConfirmReservation);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etStartTime.setOnClickListener(v -> showTimePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
        etEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    private void setupClickListeners() {
        btnConfirmReservation.setOnClickListener(v -> confirmReservation());
    }

    private void showDatePicker(boolean isStart) {
        Calendar calendar = isStart ? startCalendar : endCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    if (!isStart && calendar.before(startCalendar)) {
                        Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                        calendar.setTime(startCalendar.getTime());
                    }
                    updateDateTimeDisplays();
                    calculateTotalPrice();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar calendar = isStart ? startCalendar : endCalendar;
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    if (!isStart && calendar.before(startCalendar)) {
                        Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                        calendar.setTime(startCalendar.getTime());
                    }
                    updateDateTimeDisplays();
                    calculateTotalPrice();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void updateDateTimeDisplays() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        etStartDate.setText(dateFormat.format(startCalendar.getTime()));
        etStartTime.setText(timeFormat.format(startCalendar.getTime()));
        etEndDate.setText(dateFormat.format(endCalendar.getTime()));
        etEndTime.setText(timeFormat.format(endCalendar.getTime()));
    }

    private void calculateTotalPrice() {
        long hours = (endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis())
                / (1000 * 60 * 60);
        if (hours <= 0) hours = 1;

        double total = slot.getPricePerHour() * hours;
        tvTotalPrice.setText(String.format("Total: LKR %.2f", total));
    }

    private void confirmReservation() {
        if (startCalendar.getTime().before(Calendar.getInstance().getTime())) {
            Toast.makeText(this, "Start time cannot be in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endCalendar.before(startCalendar)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        String startTime = new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, Locale.getDefault())
                .format(startCalendar.getTime());
        String endTime = new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT, Locale.getDefault())
                .format(endCalendar.getTime());

        Call<JsonObject> call = apiInterface.createReservation(
                sessionManager.getUserId(),
                slot.getId(),
                startTime,
                endTime
        );

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    String message = data.has("message") ? data.get("message").getAsString() : "";

                    if (message.contains("created") || message.contains("success")) {

                        int reservationId = data.has("reservationId") ? data.get("reservationId").getAsInt() : 0;

                        long hours = (endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis()) / (1000 * 60 * 60);
                        if (hours <= 0) hours = 1;
                        double totalAmount = slot.getPricePerHour() * hours;

                        Intent intent = new Intent(ReservationActivity.this, PaymentActivity.class);
                        intent.putExtra("reservation_id", reservationId);
                        intent.putExtra("amount",totalAmount);

                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ReservationActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReservationActivity.this, "Reservation failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                showProgress(false);
                Toast.makeText(ReservationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnConfirmReservation.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}