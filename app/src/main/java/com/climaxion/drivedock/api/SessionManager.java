package com.climaxion.drivedock.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.climaxion.drivedock.model.User;
import com.google.gson.Gson;

public class SessionManager {

    private static final String PREF_NAME = "DriveDockPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_OBJECT = "userObject";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PHONE, user.getPhoneNumber());
        editor.putString(KEY_USER_OBJECT, gson.toJson(user));
        editor.apply();
    }

    public User getUserDetails() {
        String json = pref.getString(KEY_USER_OBJECT, null);
        if (json != null) {
            return gson.fromJson(json, User.class);
        }

        // Fallback to creating user from individual fields
        User user = new User();
        user.setId(pref.getInt(KEY_USER_ID, 0));
        String fullName = pref.getString(KEY_USER_NAME, "");
        if (fullName.contains(" ")) {
            user.setFirstName(fullName.substring(0, fullName.indexOf(" ")));
            user.setLastName(fullName.substring(fullName.indexOf(" ") + 1));
        } else {
            user.setFirstName(fullName);
            user.setLastName("");
        }
        user.setEmail(pref.getString(KEY_USER_EMAIL, ""));
        user.setPhoneNumber(pref.getString(KEY_USER_PHONE, ""));
        return user;
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, 0);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
