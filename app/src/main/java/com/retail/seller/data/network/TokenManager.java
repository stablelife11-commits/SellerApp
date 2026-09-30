package com.retail.seller.data.network;

import android.content.Context;
import android.content.SharedPreferences;
import com.retail.seller.data.model.AuthResponseDto;

public class TokenManager {

    private static final String PREF_NAME = "retail_seller_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_ROLE = "user_role";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveSession(AuthResponseDto response) {
        if (response == null) return;
        SharedPreferences.Editor editor = prefs.edit();
        if (response.getToken() != null) {
            editor.putString(KEY_TOKEN, response.getToken());
        }
        if (response.getEmail() != null) {
            editor.putString(KEY_EMAIL, response.getEmail());
        }
        if (response.getName() != null) {
            editor.putString(KEY_NAME, response.getName());
        }
        if (response.getRole() != null) {
            editor.putString(KEY_ROLE, response.getRole());
        }
        editor.apply();
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}