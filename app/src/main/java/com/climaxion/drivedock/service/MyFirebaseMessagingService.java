package com.climaxion.drivedock.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.climaxion.drivedock.R;
import com.climaxion.drivedock.activity.MainActivity;
import com.climaxion.drivedock.api.ApiClient;
import com.climaxion.drivedock.api.ApiInterface;
import com.climaxion.drivedock.api.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "drivedock_notifications";
    private static final String CHANNEL_NAME = "DriveDock Notifications";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("FCM", "Message received from: " + message.getFrom());

        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            sendNotification(title, body);
        }

        if (message.getData().size() > 0) {
            Log.d("FCM", "Data payload: " + message.getData().toString());
            String title = message.getData().get("title");
            String body = message.getData().get("body");
            if (title != null && body != null) {
                sendNotification(title, body);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token generated: " + token);

        getSharedPreferences("DriveDockPref", MODE_PRIVATE)
                .edit()
                .putString("fcmToken", token)
                .apply();
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) return;

        int userId = sessionManager.getUserId();

        ApiInterface api = ApiInterface.class.cast(ApiClient.getClient());

        api.saveFcmToken(userId, token).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                Log.d("FCM", "Token saved to backend");
            }
            @Override
            public void onFailure(retrofit2.Call<JsonObject> call, Throwable t) {
                Log.e("FCM", "Failed to save token: " + t.getMessage());
            }
        });
    }

    private void sendNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo1)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
