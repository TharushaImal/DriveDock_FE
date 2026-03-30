package com.climaxion.drivedock.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.climaxion.drivedock.R;
import com.climaxion.drivedock.api.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sessionManager = new SessionManager(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
        setContentView(R.layout.activity_splash);

        ImageView imageView = findViewById(R.id.splashLogo);

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.logo1)
                .override(300)
                .into(imageView);

        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.VISIBLE);
                    }
                },1000);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.splashProgressBar).setVisibility(View.INVISIBLE);
                if (sessionManager.isLoggedIn()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }, 2000);
    }
}