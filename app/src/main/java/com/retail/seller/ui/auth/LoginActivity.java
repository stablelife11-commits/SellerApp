package com.retail.seller.ui.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.retail.seller.MainActivity;
import com.retail.seller.R;
import com.retail.seller.data.model.AuthResponseDto;
import com.retail.seller.data.model.LoginRequestDto;
import com.retail.seller.data.model.RegisterSellerRequestDto;
import com.retail.seller.data.network.ApiClient;
import com.retail.seller.data.network.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (TokenManager.getInstance(this).isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.til_login_email);
        tilPassword = findViewById(R.id.til_login_password);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.login_progress);

        btnLogin.setOnClickListener(v -> attemptLogin());
        attachSellerRegistrationLink();
    }

    private void attachSellerRegistrationLink() {
        if (btnLogin != null && btnLogin.getParent() instanceof ViewGroup) {
            ViewGroup parentFrame = (ViewGroup) btnLogin.getParent();
            if (parentFrame.getParent() instanceof LinearLayout) {
                LinearLayout cardLayout = (LinearLayout) parentFrame.getParent();
                TextView tvRegister = new TextView(this);
                tvRegister.setText("New Wholesale Seller? Create Seller Account");
                tvRegister.setTextColor(ContextCompat.getColor(this, R.color.indigo_primary));
                tvRegister.setGravity(Gravity.CENTER);
                tvRegister.setTextSize(14f);
                int pad = (int) (16 * getResources().getDisplayMetrics().density);
                tvRegister.setPadding(pad, pad, pad, 4);
                tvRegister.setOnClickListener(v -> showSellerRegistrationDialog());
                cardLayout.addView(tvRegister);
            }
        }
    }

    private void showSellerRegistrationDialog() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, pad / 2);
        scrollView.addView(container);

        EditText etName = new EditText(this);
        etName.setHint("Owner Full Name *");
        container.addView(etName);

        EditText etBusinessName = new EditText(this);
        etBusinessName.setHint("Wholesale Business / Firm Name *");
        container.addView(etBusinessName);

        EditText etMobile = new EditText(this);
        etMobile.setHint("10-Digit Mobile Number *");
        etMobile.setInputType(InputType.TYPE_CLASS_PHONE);
        container.addView(etMobile);

        EditText etRegEmail = new EditText(this);
        etRegEmail.setHint("Business Email Address *");
        etRegEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        container.addView(etRegEmail);

        EditText etRegPassword = new EditText(this);
        etRegPassword.setHint("Password (min 6 chars) *");
        etRegPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(etRegPassword);

        EditText etGst = new EditText(this);
        etGst.setHint("GSTIN (Optional)");
        container.addView(etGst);

        EditText etAddress = new EditText(this);
        etAddress.setHint("Market / Godown Address (Optional)");
        container.addView(etAddress);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Register Wholesale Seller")
                .setView(scrollView)
                .setPositiveButton("Register", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String bizName = etBusinessName.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String email = etRegEmail.getText().toString().trim();
            String password = etRegPassword.getText().toString().trim();
            String gst = etGst.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || bizName.isEmpty()) {
                Toast.makeText(this, "Owner name and Business name are required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!mobile.matches("^\\d{10}$")) {
                Toast.makeText(this, "Mobile number must be exactly 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            setLoading(true);

            RegisterSellerRequestDto req = new RegisterSellerRequestDto(
                    name, email, password, mobile, bizName,
                    gst.isEmpty() ? null : gst,
                    address.isEmpty() ? null : address
            );

            ApiClient.getApiService(this).registerSeller(req).enqueue(new Callback<AuthResponseDto>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponseDto> call, @NonNull Response<AuthResponseDto> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                        AuthResponseDto auth = response.body();
                        TokenManager tm = TokenManager.getInstance(LoginActivity.this);
                        tm.saveSession(auth);
                        tm.saveBusinessProfile(
                                auth.getBusinessName() != null ? auth.getBusinessName() : bizName,
                                auth.getGstNumber() != null ? auth.getGstNumber() : gst,
                                auth.getBusinessAddress() != null ? auth.getBusinessAddress() : address,
                                auth.getBusinessPhone() != null ? auth.getBusinessPhone() : mobile
                        );
                        Toast.makeText(LoginActivity.this, "Seller account created! Welcome, " + auth.getName(), Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String err = ApiClient.extractErrorMessage(response, "Registration failed");
                        Toast.makeText(LoginActivity.this, err, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponseDto> call, @NonNull Throwable t) {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }));

        dialog.show();
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        LoginRequestDto request = new LoginRequestDto(email, password);

        ApiClient.getApiService(this).login(request).enqueue(new Callback<AuthResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponseDto> call, @NonNull Response<AuthResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                    AuthResponseDto auth = response.body();
                    String role = auth.getRole() != null ? auth.getRole().trim().toUpperCase() : "";

                    if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
                        Toast.makeText(LoginActivity.this,
                                "Access Denied: This account has role '" + role + "'. Only SELLER or ADMIN accounts can use the Seller App.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    TokenManager tm = TokenManager.getInstance(LoginActivity.this);
                    tm.saveSession(auth);
                    tm.saveBusinessProfile(
                            auth.getBusinessName() != null ? auth.getBusinessName() : "",
                            auth.getGstNumber() != null ? auth.getGstNumber() : "",
                            auth.getBusinessAddress() != null ? auth.getBusinessAddress() : "",
                            auth.getBusinessPhone() != null ? auth.getBusinessPhone() : ""
                    );
                    Toast.makeText(LoginActivity.this, "Welcome, " + auth.getName() + "!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    String errorMsg = ApiClient.extractErrorMessage(response, "Invalid email or password");
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponseDto> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Cannot connect to server: " + t.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
        }
    }
}