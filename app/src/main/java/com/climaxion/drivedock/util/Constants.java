package com.climaxion.drivedock.util;

import com.climaxion.drivedock.BuildConfig;

public class Constants {

    // API URLs
    public static final String BASE_URL = BuildConfig.BASE_URL;

    // Shared Preferences Keys
    public static final String PREF_NAME = "DriveDockPref";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_USER_EMAIL = "userEmail";
    public static final String KEY_FCM_TOKEN = "fcmToken";

    // Intent Extras
    public static final String EXTRA_LOCATION_ID = "location_id";
    public static final String EXTRA_LOCATION_NAME = "location_name";
    public static final String EXTRA_SLOT_ID = "slot_id";
    public static final String EXTRA_SLOT_NUMBER = "slot_number";
    public static final String EXTRA_PRICE_PER_HOUR = "price_per_hour";
    public static final String EXTRA_RESERVATION_ID = "reservation_id";
    public static final String EXTRA_RESERVATION = "reservation";
    public static final String EXTRA_SERVICE = "service";

    // Request Codes
    public static final int LOCATION_PERMISSION_REQUEST = 1001;
    public static final int PAYMENT_REQUEST_CODE = 1002;

    // Date Format
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy, HH:mm";
}
