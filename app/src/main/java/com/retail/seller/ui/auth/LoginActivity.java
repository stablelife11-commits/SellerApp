package com.retail.seller.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.retail.seller.MainActivity;
import com.retail.seller.R;
import com.retail.seller.data.model.AuthResponseDto;
import com.retail.seller.data.model.LoginRequestDto;
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

                    // Enforce Seller/Admin role check on Seller App
                    if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
                        Toast.makeText(LoginActivity.this,
                                "Access Denied: This account has role '" + role + "'. Only SELLER or ADMIN accounts can use the Seller App.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    TokenManager.getInstance(LoginActivity.this).saveSession(auth);
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
                        "Cannot connect to server. Verify backend is running and IP matches ApiClient: " + t.getLocalizedMessage(),
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