package com.retail.seller.data.network;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.retail.seller.data.model.AuthResponseDto;

public class TokenManager {

    private static final String PREF_NAME = "retail_seller_session_secure";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_ROLE = "user_role";

    // Business Profile & Settings Keys
    private static final String KEY_BUSINESS_NAME = "seller_business_name";
    private static final String KEY_GST_NUMBER = "seller_gst_number";
    private static final String KEY_BUSINESS_ADDRESS = "seller_business_address";
    private static final String KEY_BUSINESS_PHONE = "seller_business_phone";
    private static final String KEY_NOTIFY_ORDERS = "pref_notify_orders";
    private static final String KEY_NOTIFY_LOW_STOCK = "pref_notify_low_stock";
    private static final String KEY_APP_LANGUAGE = "pref_app_language";
    private static final String KEY_DARK_MODE = "pref_dark_mode";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    private TokenManager(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences initializedPrefs;
        try {
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            initializedPrefs = EncryptedSharedPreferences.create(
                    appContext,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            initializedPrefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
        prefs = initializedPrefs;
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
        if (response.getUserId() != null) {
            editor.putLong(KEY_USER_ID, response.getUserId());
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

    public Long getUserId() {
        long id = prefs.getLong(KEY_USER_ID, -1L);
        return id > 0 ? id : null;
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

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }

    public boolean isSellerOrAdmin() {
        String role = getRole();
        return "SELLER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty() && getUserId() != null && isSellerOrAdmin();
    }

    public void saveBusinessProfile(String businessName, String gstNumber, String address, String phone) {
        prefs.edit()
                .putString(KEY_BUSINESS_NAME, businessName)
                .putString(KEY_GST_NUMBER, gstNumber)
                .putString(KEY_BUSINESS_ADDRESS, address)
                .putString(KEY_BUSINESS_PHONE, phone)
                .apply();
    }

    public String getBusinessName() {
        return prefs.getString(KEY_BUSINESS_NAME, "Wholesale Garments & Innerwear Hub");
    }

    public String getGstNumber() {
        return prefs.getString(KEY_GST_NUMBER, "09AAACH7409R1ZP");
    }

    public String getBusinessAddress() {
        return prefs.getString(KEY_BUSINESS_ADDRESS, "Main Wholesale Market, Uttar Pradesh, India");
    }

    public String getBusinessPhone() {
        return prefs.getString(KEY_BUSINESS_PHONE, "+91 9876543210");
    }

    public boolean isOrderNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFY_ORDERS, true);
    }

    public void setOrderNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFY_ORDERS, enabled).apply();
    }

    public boolean isLowStockAlertsEnabled() {
        return prefs.getBoolean(KEY_NOTIFY_LOW_STOCK, true);
    }

    public void setLowStockAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFY_LOW_STOCK, enabled).apply();
    }

    public String getAppLanguage() {
        return prefs.getString(KEY_APP_LANGUAGE, "English (India)");
    }

    public void setAppLanguage(String language) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public void clearSession() {
        boolean darkMode = isDarkModeEnabled();
        String language = getAppLanguage();
        prefs.edit()
                .clear()
                .putBoolean(KEY_DARK_MODE, darkMode)
                .putString(KEY_APP_LANGUAGE, language)
                .apply();
    }
}