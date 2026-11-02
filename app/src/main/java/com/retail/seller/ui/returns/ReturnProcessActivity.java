package com.retail.seller.ui.returns;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.retail.seller.R;
import com.retail.seller.data.model.ReturnItemRequestDto;
import com.retail.seller.data.model.ReturnRequestDto;
import com.retail.seller.data.model.ReturnResponseDto;
import com.retail.seller.data.model.SaleItemDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.network.ApiClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReturnProcessActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_JSON = "EXTRA_ORDER_JSON";

    private RadioGroup rgReturnType;
    private RadioButton rbOnlineOrder, rbPosSale;
    private TextInputLayout tilReferenceId;
    private TextInputEditText etSaleId, etVariantId, etQuantity, etReason;
    private LinearLayout layoutOrderItemsSection, layoutOrderItemsList, layoutManualVariantInputs;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;
    private MaterialCardView cardSuccessResult;
    private TextView tvResultDetails;

    private final List<OrderLineReturnRow> itemReturnRows = new ArrayList<>();
    private boolean isMultiItemOrderMode = false;

    private static class OrderLineReturnRow {
        final Long variantId;
        final String productName;
        final int maxPurchasedQty;
        final EditText etReturnQty;

        OrderLineReturnRow(Long variantId, String productName, int maxPurchasedQty, EditText etReturnQty) {
            this.variantId = variantId;
            this.productName = productName;
            this.maxPurchasedQty = maxPurchasedQty;
            this.etReturnQty = etReturnQty;
        }
    }

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

        layoutOrderItemsSection = findViewById(R.id.layout_order_items_section);
        layoutOrderItemsList = findViewById(R.id.layout_order_items_return_list);
        layoutManualVariantInputs = findViewById(R.id.layout_manual_variant_inputs);

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
            if (getIntent().hasExtra(EXTRA_ORDER_JSON)) {
                String orderJson = getIntent().getStringExtra(EXTRA_ORDER_JSON);
                if (!TextUtils.isEmpty(orderJson)) {
                    try {
                        SaleResponseDto order = new Gson().fromJson(orderJson, SaleResponseDto.class);
                        if (order != null && order.getId() != null) {
                            rbOnlineOrder.setChecked(true);
                            etSaleId.setText(String.valueOf(order.getId()));
                            etSaleId.setEnabled(false);
                            rbOnlineOrder.setEnabled(false);
                            rbPosSale.setEnabled(false);
                            if (order.getItems() != null && !order.getItems().isEmpty()) {
                                populateOrderItemsForReturn(order.getItems());
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            if (!isMultiItemOrderMode) {
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
        }

        btnSubmit.setOnClickListener(v -> attemptSubmitReturn());
    }

    private void populateOrderItemsForReturn(List<SaleItemDto> items) {
        isMultiItemOrderMode = true;
        layoutManualVariantInputs.setVisibility(View.GONE);
        layoutOrderItemsSection.setVisibility(View.VISIBLE);
        layoutOrderItemsList.removeAllViews();
        itemReturnRows.clear();

        float density = getResources().getDisplayMetrics().density;

        for (SaleItemDto item : items) {
            if (item.getVariantId() == null) continue;

            LinearLayout rowCard = new LinearLayout(this);
            rowCard.setOrientation(LinearLayout.HORIZONTAL);
            rowCard.setGravity(Gravity.CENTER_VERTICAL);
            rowCard.setBackgroundResource(R.drawable.bg_chip);
            int pad = (int) (12 * density);
            rowCard.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = (int) (8 * density);
            rowCard.setLayoutParams(params);

            LinearLayout infoCol = new LinearLayout(this);
            infoCol.setOrientation(LinearLayout.VERTICAL);
            infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvTitle = new TextView(this);
            String size = item.getSize() != null && !item.getSize().isEmpty() ? item.getSize() : "Std";
            String color = item.getColor() != null && !item.getColor().isEmpty() ? item.getColor() : "Std";
            tvTitle.setText(String.format(Locale.getDefault(), "%s (%s | %s)",
                    item.getProductName(), size, color));
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setTextSize(13.5f);
            infoCol.addView(tvTitle);

            TextView tvSub = new TextView(this);
            tvSub.setText(String.format(Locale.getDefault(),
                    "Variant ID: #%d • Sold Qty: %d × ₹%.2f",
                    item.getVariantId(), item.getQuantity(), item.getPrice()));
            tvSub.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvSub.setTextSize(12f);
            infoCol.addView(tvSub);

            rowCard.addView(infoCol);

            EditText etQty = new EditText(this);
            etQty.setHint("0 / " + item.getQuantity());
            etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
            etQty.setGravity(Gravity.CENTER);
            etQty.setTextSize(14f);
            LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(
                    (int) (78 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
            qtyParams.leftMargin = (int) (8 * density);
            etQty.setLayoutParams(qtyParams);

            rowCard.addView(etQty);
            layoutOrderItemsList.addView(rowCard);

            itemReturnRows.add(new OrderLineReturnRow(
                    item.getVariantId(),
                    item.getProductName(),
                    item.getQuantity(),
                    etQty
            ));
        }
    }

    private void attemptSubmitReturn() {
        String refIdStr = etSaleId.getText() != null ? etSaleId.getText().toString().trim() : "";
        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";
        boolean isOnlineOrder = rbOnlineOrder.isChecked();

        if (TextUtils.isEmpty(refIdStr)) {
            etSaleId.setError((isOnlineOrder ? "Order ID" : "Sale ID") + " is required");
            etSaleId.requestFocus();
            return;
        }

        long referenceId;
        try {
            referenceId = Long.parseLong(refIdStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Invalid Order/Sale ID", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ReturnItemRequestDto> returnItems = new ArrayList<>();

        if (isMultiItemOrderMode && !itemReturnRows.isEmpty()) {
            for (OrderLineReturnRow row : itemReturnRows) {
                String qStr = row.etReturnQty.getText() != null ? row.etReturnQty.getText().toString().trim() : "";
                if (qStr.isEmpty()) continue;
                try {
                    int qty = Integer.parseInt(qStr);
                    if (qty < 0) {
                        row.etReturnQty.setError("Invalid");
                        return;
                    }
                    if (qty > row.maxPurchasedQty) {
                        row.etReturnQty.setError("Max " + row.maxPurchasedQty);
                        Toast.makeText(this,
                                "Return qty (" + qty + ") exceeds purchased qty (" + row.maxPurchasedQty + ") for " + row.productName,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (qty > 0) {
                        returnItems.add(new ReturnItemRequestDto(row.variantId, qty));
                    }
                } catch (NumberFormatException ex) {
                    row.etReturnQty.setError("Invalid number");
                    return;
                }
            }

            if (returnItems.isEmpty()) {
                Toast.makeText(this, "Enter a return quantity (> 0) for at least one item.", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            String variantIdStr = etVariantId.getText() != null ? etVariantId.getText().toString().trim() : "";
            String qtyStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";

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

            try {
                long variantId = Long.parseLong(variantIdStr);
                int quantity = Integer.parseInt(qtyStr);
                if (quantity < 1) {
                    etQuantity.setError("Quantity must be at least 1");
                    return;
                }
                returnItems.add(new ReturnItemRequestDto(variantId, quantity));
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Please enter valid numeric Variant ID and quantity.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ReturnRequestDto request = isOnlineOrder
                ? ReturnRequestDto.forOnlineOrder(referenceId, reason, returnItems)
                : ReturnRequestDto.forPosSale(referenceId, reason, returnItems);

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
                    "Return #%d | %s\nRestored Qty: %d | Refund / Credit Value: ₹%.2f\nReason: %s",
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