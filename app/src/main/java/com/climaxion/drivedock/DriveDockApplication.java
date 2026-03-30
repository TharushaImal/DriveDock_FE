package com.climaxion.drivedock;

import android.app.Application;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;

public class DriveDockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
    }
}
