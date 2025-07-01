package com.example.festiv.Models;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.festiv.Activities.ReservationActivity;

public class UserSession {
    private static final String PREF_NAME = "FestivUserSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_TOKEN = "authToken";
    private static final String KEY_EMAIL = "userEmail";
    private static final String KEY_NAME = "userName";

    private static SharedPreferences sharedPreferences;

    public UserSession(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Save user ID
    public static void saveUserId(Context context, Long userId) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.apply();
    }

    // Get user ID
    public static Long getUserId(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_USER_ID, 0L);
    }

    // Save auth token
    public void setAuthToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Save email
    public void setUserEmail(String email) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply();
    }

    public static String getUserEmail(ReservationActivity reservationActivity) {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    // Save user name
    public void setUserName(String name) {
        sharedPreferences.edit().putString(KEY_NAME, name).apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_NAME, null);
    }

    // Check login status
    public static boolean isLoggedIn(Context context) {
        return getUserId(context) > 0L;
    }

    // Clear session
    public static void clearSession(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
