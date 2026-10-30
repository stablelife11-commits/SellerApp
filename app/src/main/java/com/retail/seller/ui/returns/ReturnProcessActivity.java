package com.retail.seller.ui.returns;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

    private RadioGroup rgReturnType;
    private RadioButton rbOnlineOrder, rbPosSale;
    private TextInputLayout tilReferenceId;
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

        rgReturnType = findViewById(R.id.rg_return_type);
        rbOnlineOrder = findViewById(R.id.rb_return_online_order);
        rbPosSale = findViewById(R.id.rb_return_pos_sale);
        tilReferenceId = findViewById(R.id.til_return_sale_id);

        etSaleId = findViewById(R.id.et_return_sale_id);
        etVariantId = findViewById(R.id.et_return_variant_id);
        etQuantity = findViewById(R.id.et_return_quantity);
        etReason = findViewById(R.id.et_return_reason);

        btnSubmit = findViewById(R.id.btn_submit_return);
        progressBar = findViewById(R.id.return_progress);
        cardSuccessResult = findViewById(R.id.card_return_success_result);
        tvResultDetails = findViewById(R.id.tv_return_result_details);

        rgReturnType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_return_online_order) {
                tilReferenceId.setHint("Delivered Online Order ID *");
            } else {
                tilReferenceId.setHint("POS Sale ID *");
            }
        });

        if (getIntent() != null) {
            if (getIntent().hasExtra("EXTRA_ORDER_ID")) {
                long orderId = getIntent().getLongExtra("EXTRA_ORDER_ID", 0);
                if (orderId > 0) {
                    rbOnlineOrder.setChecked(true);
                    etSaleId.setText(String.valueOf(orderId));
                }
            } else if (getIntent().hasExtra("EXTRA_SALE_ID")) {
                long saleId = getIntent().getLongExtra("EXTRA_SALE_ID", 0);
                if (saleId > 0) {
                    rbPosSale.setChecked(true);
                    etSaleId.setText(String.valueOf(saleId));
                }
            }

            if (getIntent().hasExtra("EXTRA_VARIANT_ID")) {
                long variantId = getIntent().getLongExtra("EXTRA_VARIANT_ID", 0);
                if (variantId > 0) {
                    etVariantId.setText(String.valueOf(variantId));
                }
            }
        }

        btnSubmit.setOnClickListener(v -> attemptSubmitReturn());
    }

    private void attemptSubmitReturn() {
        String refIdStr = etSaleId.getText() != null ? etSaleId.getText().toString().trim() : "";
        String variantIdStr = etVariantId.getText() != null ? etVariantId.getText().toString().trim() : "";
        String qtyStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";

        boolean isOnlineOrder = rbOnlineOrder.isChecked();

        if (TextUtils.isEmpty(refIdStr)) {
            etSaleId.setError((isOnlineOrder ? "Order ID" : "Sale ID") + " is required");
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

        long referenceId;
        long variantId;
        int quantity;
        try {
            referenceId = Long.parseLong(refIdStr);
            variantId = Long.parseLong(variantIdStr);
            quantity = Integer.parseInt(qtyStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Please enter valid numeric IDs and quantity.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantity < 1) {
            etQuantity.setError("Quantity must be at least 1");
            return;
        }

        ReturnItemRequestDto returnItem = new ReturnItemRequestDto(variantId, quantity);
        ReturnRequestDto request = isOnlineOrder
                ? ReturnRequestDto.forOnlineOrder(referenceId, reason, Collections.singletonList(returnItem))
                : ReturnRequestDto.forPosSale(referenceId, reason, Collections.singletonList(returnItem));

        setLoading(true);
        cardSuccessResult.setVisibility(View.GONE);

        ApiClient.getApiService(this).processReturn(request).enqueue(new Callback<ReturnResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<ReturnResponseDto> call, @NonNull Response<ReturnResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    showSuccessResult(response.body());
                } else {
                    String errorMsg = ApiClient.extractErrorMessage(response, "Failed to process return");
                    Toast.makeText(ReturnProcessActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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
            String refLabel = response.getOrderId() != null
                    ? "Order ID: #" + response.getOrderId()
                    : "Sale ID: #" + response.getSaleId();

            String details = String.format(Locale.getDefault(),
                    "Return #%d | %s\nRestored Qty: %d | Refund Value: ₹%.2f\nReason: %s",
                    response.getId() != null ? response.getId() : 0,
                    refLabel,
                    response.getQuantityRestored(),
                    response.getTotalReturnAmount(),
                    response.getReason() != null ? response.getReason() : "N/A");
            tvResultDetails.setText(details);
            cardSuccessResult.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, "Return processed & inventory restored!", Toast.LENGTH_LONG).show();
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