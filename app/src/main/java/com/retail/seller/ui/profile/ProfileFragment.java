package com.retail.seller.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.retail.seller.R;
import com.retail.seller.data.network.TokenManager;
import com.retail.seller.ui.auth.LoginActivity;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView tvInitials, tvSellerName, tvSellerEmail, tvRoleBadge, tvSelectedLanguage;
    private TextInputEditText etBusinessName, etGst, etPhone, etAddress;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvInitials = view.findViewById(R.id.tv_profile_initials);
        tvSellerName = view.findViewById(R.id.tv_profile_seller_name);
        tvSellerEmail = view.findViewById(R.id.tv_profile_seller_email);
        tvRoleBadge = view.findViewById(R.id.tv_profile_role_badge);
        tvSelectedLanguage = view.findViewById(R.id.tv_selected_language);

        etBusinessName = view.findViewById(R.id.et_profile_business_name);
        etGst = view.findViewById(R.id.et_profile_gst);
        etPhone = view.findViewById(R.id.et_profile_phone);
        etAddress = view.findViewById(R.id.et_profile_address);

        MaterialButton btnSaveBusiness = view.findViewById(R.id.btn_save_business_profile);
        MaterialButton btnLogout = view.findViewById(R.id.btn_profile_logout);
        MaterialSwitch switchOrders = view.findViewById(R.id.switch_notify_orders);
        MaterialSwitch switchLowStock = view.findViewById(R.id.switch_notify_low_stock);
        MaterialSwitch switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        LinearLayout layoutLanguage = view.findViewById(R.id.layout_language_selector);

        TokenManager tm = TokenManager.getInstance(requireContext());
        String name = tm.getName() != null ? tm.getName() : "Wholesale Seller";
        String email = tm.getEmail() != null ? tm.getEmail() : "seller@retailflow.com";
        String role = tm.getRole() != null ? tm.getRole() : "SELLER";
        Long userId = tm.getUserId();

        tvSellerName.setText(name);
        tvSellerEmail.setText(email + (userId != null ? " • Seller ID #" + userId : ""));
        tvRoleBadge.setText("VERIFIED B2B " + role);
        tvInitials.setText(extractInitials(name));

        etBusinessName.setText(tm.getBusinessName());
        etGst.setText(tm.getGstNumber());
        etPhone.setText(tm.getBusinessPhone());
        etAddress.setText(tm.getBusinessAddress());

        switchOrders.setChecked(tm.isOrderNotificationsEnabled());
        switchLowStock.setChecked(tm.isLowStockAlertsEnabled());
        switchDarkMode.setChecked(tm.isDarkModeEnabled());
        tvSelectedLanguage.setText(tm.getAppLanguage());

        btnSaveBusiness.setOnClickListener(v -> {
            String bName = etBusinessName.getText() != null ? etBusinessName.getText().toString().trim() : "";
            String gst = etGst.getText() != null ? etGst.getText().toString().trim().toUpperCase(Locale.ROOT) : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String addr = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

            if (bName.isEmpty()) {
                etBusinessName.setError("Business name is required");
                return;
            }
            tm.saveBusinessProfile(bName, gst, addr, phone);
            Toast.makeText(requireContext(), "Business & GST profile saved!", Toast.LENGTH_SHORT).show();
        });

        switchOrders.setOnCheckedChangeListener((btn, isChecked) -> tm.setOrderNotificationsEnabled(isChecked));
        switchLowStock.setOnCheckedChangeListener((btn, isChecked) -> tm.setLowStockAlertsEnabled(isChecked));
        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            tm.setDarkModeEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        layoutLanguage.setOnClickListener(v -> {
            String[] languages = {"English (India)", "हिन्दी (Hindi)"};
            new AlertDialog.Builder(requireContext())
                    .setTitle("Select Application Language")
                    .setItems(languages, (d, which) -> {
                        tm.setAppLanguage(languages[which]);
                        tvSelectedLanguage.setText(languages[which]);
                        Toast.makeText(requireContext(), "Language set to " + languages[which], Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

        btnLogout.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to sign out of your seller account?")
                .setPositiveButton("Log Out", (d, w) -> {
                    tm.clearSession();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show());
    }

    private String extractInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "RS";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ROOT);
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase(Locale.ROOT);
    }
}