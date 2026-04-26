package com.example.protypebillingsystem;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "MediPayPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save JWT token
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Get JWT token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Clear token on logout
    public void clearToken() {
        editor.remove(KEY_TOKEN);
        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
