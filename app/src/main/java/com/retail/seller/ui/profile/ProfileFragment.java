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
import com.retail.seller.data.model.UserProfileDto;
import com.retail.seller.data.network.ApiClient;
import com.retail.seller.data.network.TokenManager;
import com.retail.seller.ui.auth.LoginActivity;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvInitials, tvSellerName, tvSellerEmail, tvRoleBadge, tvSelectedLanguage;
    private TextInputEditText etBusinessName, etGst, etPhone, etAddress;
    private MaterialButton btnSaveBusiness;

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

        btnSaveBusiness = view.findViewById(R.id.btn_save_business_profile);
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
        tvRoleBadge.setText(tm.isAdmin() ? "ADMIN • TAP TO MANAGE USERS" : "VERIFIED B2B " + role);
        tvInitials.setText(extractInitials(name));

        if (tm.isAdmin()) {
            tvRoleBadge.setOnClickListener(v -> openAdminUserManagementDialog());
        }

        etBusinessName.setText(tm.getBusinessName());
        etGst.setText(tm.getGstNumber());
        etPhone.setText(tm.getBusinessPhone());
        etAddress.setText(tm.getBusinessAddress());

        switchOrders.setChecked(tm.isOrderNotificationsEnabled());
        switchLowStock.setChecked(tm.isLowStockAlertsEnabled());
        switchDarkMode.setChecked(tm.isDarkModeEnabled());
        tvSelectedLanguage.setText(tm.getAppLanguage());

        fetchProfileFromServer(tm);

        btnSaveBusiness.setOnClickListener(v -> saveProfileToServer(tm));

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

    private void fetchProfileFromServer(TokenManager tm) {
        ApiClient.getApiService(requireContext()).getMyProfile().enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileDto> call, @NonNull Response<UserProfileDto> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                UserProfileDto p = response.body();
                String bName = p.getBusinessName() != null && !p.getBusinessName().isEmpty() ? p.getBusinessName() : tm.getBusinessName();
                String gst = p.getGstNumber() != null && !p.getGstNumber().isEmpty() ? p.getGstNumber() : tm.getGstNumber();
                String phone = p.getBusinessPhone() != null && !p.getBusinessPhone().isEmpty() ? p.getBusinessPhone() : tm.getBusinessPhone();
                String addr = p.getBusinessAddress() != null && !p.getBusinessAddress().isEmpty() ? p.getBusinessAddress() : tm.getBusinessAddress();

                tm.saveBusinessProfile(bName, gst, addr, phone);
                etBusinessName.setText(bName);
                etGst.setText(gst);
                etPhone.setText(phone);
                etAddress.setText(addr);
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileDto> call, @NonNull Throwable t) {
            }
        });
    }

    private void saveProfileToServer(TokenManager tm) {
        String bName = etBusinessName.getText() != null ? etBusinessName.getText().toString().trim() : "";
        String gst = etGst.getText() != null ? etGst.getText().toString().trim().toUpperCase(Locale.ROOT) : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String addr = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";

        if (bName.isEmpty()) {
            etBusinessName.setError("Business name is required");
            return;
        }

        btnSaveBusiness.setEnabled(false);
        UserProfileDto updateReq = new UserProfileDto(
                tm.getName() != null ? tm.getName() : bName,
                bName,
                gst,
                phone,
                addr
        );

        ApiClient.getApiService(requireContext()).updateMyProfile(updateReq).enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileDto> call, @NonNull Response<UserProfileDto> response) {
                if (!isAdded()) return;
                btnSaveBusiness.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    tm.saveBusinessProfile(bName, gst, addr, phone);
                    Toast.makeText(requireContext(), "Business & GST profile synced to server!", Toast.LENGTH_SHORT).show();
                } else {
                    String err = ApiClient.extractErrorMessage(response, "Could not update profile on server");
                    Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnSaveBusiness.setEnabled(true);
                tm.saveBusinessProfile(bName, gst, addr, phone);
                Toast.makeText(requireContext(), "Saved locally (offline): " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAdminUserManagementDialog() {
        ApiClient.getApiService(requireContext()).getAllUsersForAdmin().enqueue(new Callback<List<UserProfileDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserProfileDto>> call, @NonNull Response<List<UserProfileDto>> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(requireContext(), "Could not load platform users.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<UserProfileDto> users = response.body();
                String[] labels = new String[users.size()];
                for (int i = 0; i < users.size(); i++) {
                    UserProfileDto u = users.get(i);
                    labels[i] = String.format(Locale.getDefault(), "#%d %s (%s) — Role: %s [%s]",
                            u.getId(), u.getName(), u.getEmail(), u.getRole(),
                            Boolean.TRUE.equals(u.getActive()) ? "ACTIVE" : "INACTIVE");
                }

                new AlertDialog.Builder(requireContext())
                        .setTitle("Platform Users — Tap to Change Role")
                        .setItems(labels, (d, which) -> showRoleChangePicker(users.get(which)))
                        .setNegativeButton("Close", null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<List<UserProfileDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRoleChangePicker(UserProfileDto targetUser) {
        String[] roles = {"SELLER", "BUYER", "ADMIN"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Assign Role for " + targetUser.getName())
                .setItems(roles, (d, which) -> {
                    String selectedRole = roles[which];
                    ApiClient.getApiService(requireContext())
                            .updateUserRole(targetUser.getId(), selectedRole)
                            .enqueue(new Callback<UserProfileDto>() {
                                @Override
                                public void onResponse(@NonNull Call<UserProfileDto> call, @NonNull Response<UserProfileDto> response) {
                                    if (!isAdded()) return;
                                    if (response.isSuccessful()) {
                                        Toast.makeText(requireContext(),
                                                targetUser.getName() + " updated to " + selectedRole,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(),
                                                ApiClient.extractErrorMessage(response, "Failed to update role"),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<UserProfileDto> call, @NonNull Throwable t) {
                                    if (!isAdded()) return;
                                    Toast.makeText(requireContext(), "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
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