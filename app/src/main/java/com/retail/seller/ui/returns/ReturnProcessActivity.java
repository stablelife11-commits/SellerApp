package com.retail.seller.ui.returns;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.retail.seller.R;
import com.retail.seller.data.model.ReturnItemRequestDto;
import com.retail.seller.data.model.ReturnRequestDto;
import com.retail.seller.data.model.ReturnResponseDto;
import com.retail.seller.data.network.ApiClient;
import java.util.Collections;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReturnProcessActivity extends AppCompatActivity {

    private TextInputEditText etSaleId, etVariantId, etQuantity, etReason;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;
    private MaterialCardView cardSuccessResult;
    private TextView tvResultDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_process);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_return_process);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etSaleId = findViewById(R.id.et_return_sale_id);
        etVariantId = findViewById(R.id.et_return_variant_id);
        etQuantity = findViewById(R.id.et_return_quantity);
        etReason = findViewById(R.id.et_return_reason);

        btnSubmit = findViewById(R.id.btn_submit_return);
        progressBar = findViewById(R.id.return_progress);
        cardSuccessResult = findViewById(R.id.card_return_success_result);
        tvResultDetails = findViewById(R.id.tv_return_result_details);

        if (getIntent() != null && getIntent().hasExtra("EXTRA_SALE_ID")) {
            long saleId = getIntent().getLongExtra("EXTRA_SALE_ID", 0);
            if (saleId > 0) {
                etSaleId.setText(String.valueOf(saleId));
            }
        }

        btnSubmit.setOnClickListener(v -> attemptSubmitReturn());
    }

    private void attemptSubmitReturn() {
        String saleIdStr = etSaleId.getText() != null ? etSaleId.getText().toString().trim() : "";
        String variantIdStr = etVariantId.getText() != null ? etVariantId.getText().toString().trim() : "";
        String qtyStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";

        if (TextUtils.isEmpty(saleIdStr)) {
            etSaleId.setError("Sale ID is required");
            etSaleId.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(variantIdStr)) {
            etVariantId.setError("Variant ID is required");
            etVariantId.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(qtyStr)) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        long saleId = Long.parseLong(saleIdStr);
        long variantId = Long.parseLong(variantIdStr);
        int quantity = Integer.parseInt(qtyStr);

        ReturnItemRequestDto returnItem = new ReturnItemRequestDto(variantId, quantity);
        ReturnRequestDto request = new ReturnRequestDto(saleId, reason, Collections.singletonList(returnItem));

        setLoading(true);

        ApiClient.getApiService(this).processReturn(request).enqueue(new Callback<ReturnResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<ReturnResponseDto> call, @NonNull Response<ReturnResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showSuccessResult(response.body());
                } else {
                    Toast.makeText(ReturnProcessActivity.this, "Server error processing return: Code " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReturnResponseDto> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(ReturnProcessActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessResult(ReturnResponseDto response) {
        if (cardSuccessResult != null && tvResultDetails != null) {
            String details = String.format(Locale.getDefault(),
                    "Return ID: %d | Sale ID: %d\nReason: %s",
                    response.getId() != null ? response.getId() : 0,
                    response.getSaleId() != null ? response.getSaleId() : 0,
                    response.getReason() != null ? response.getReason() : "N/A");
            tvResultDetails.setText(details);
            cardSuccessResult.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, "Customer return processed on server!", Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSubmit != null) {
            btnSubmit.setEnabled(!isLoading);
        }
    }
}