package com.retail.seller.ui.products;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductRequestDto;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantRequestDto;
import com.retail.seller.data.network.ApiClient;
import java.util.Collections;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText etName, etSku, etCategory, etBrand, etDescription, etImageUrl;
    private TextInputEditText etVarSku, etVarSize, etVarColor, etSellingPrice, etPurchasePrice, etInitialStock;
    private MaterialButton btnSave;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_product);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.et_product_name);
        etSku = findViewById(R.id.et_product_sku);
        etCategory = findViewById(R.id.et_category);
        etBrand = findViewById(R.id.et_brand);
        etDescription = findViewById(R.id.et_description);
        etImageUrl = findViewById(R.id.et_image_url);

        etVarSku = findViewById(R.id.et_variant_sku);
        etVarSize = findViewById(R.id.et_variant_size);
        etVarColor = findViewById(R.id.et_variant_color);
        etSellingPrice = findViewById(R.id.et_selling_price);
        etPurchasePrice = findViewById(R.id.et_purchase_price);
        etInitialStock = findViewById(R.id.et_initial_stock);

        btnSave = findViewById(R.id.btn_save_product);
        progressBar = findViewById(R.id.add_product_progress);

        btnSave.setOnClickListener(v -> attemptSaveProduct());
    }

    private void attemptSaveProduct() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String sku = etSku.getText() != null ? etSku.getText().toString().trim() : "";
        String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";
        String brand = etBrand.getText() != null ? etBrand.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String imageUrl = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";

        String varSku = etVarSku.getText() != null ? etVarSku.getText().toString().trim() : "";
        String varSize = etVarSize.getText() != null ? etVarSize.getText().toString().trim() : "";
        String varColor = etVarColor.getText() != null ? etVarColor.getText().toString().trim() : "";
        String sellingPriceStr = etSellingPrice.getText() != null ? etSellingPrice.getText().toString().trim() : "";
        String purchasePriceStr = etPurchasePrice.getText() != null ? etPurchasePrice.getText().toString().trim() : "";
        String stockStr = etInitialStock.getText() != null ? etInitialStock.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etName.setError("Product name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(sku)) {
            etSku.setError("Product SKU is required");
            etSku.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(category)) {
            etCategory.setError("Category is required");
            etCategory.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(varSku)) {
            etVarSku.setError("Variant SKU is required");
            etVarSku.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(sellingPriceStr)) {
            etSellingPrice.setError("Selling price is required");
            etSellingPrice.requestFocus();
            return;
        }

        double sellingPrice = Double.parseDouble(sellingPriceStr);
        double purchasePrice = !TextUtils.isEmpty(purchasePriceStr) ? Double.parseDouble(purchasePriceStr) : 0.0;
        int stock = !TextUtils.isEmpty(stockStr) ? Integer.parseInt(stockStr) : 0;

        ProductVariantRequestDto variantReq = new ProductVariantRequestDto(
                varSku, varSize, varColor, sellingPrice, purchasePrice, stock
        );

        Long defaultSellerId = 1L;
        ProductRequestDto productReq = new ProductRequestDto(
                defaultSellerId, sku, name, description, category, brand,
                Collections.singletonList(variantReq),
                !imageUrl.isEmpty() ? Collections.singletonList(imageUrl) : Collections.emptyList()
        );

        setLoading(true);

        ApiClient.getApiService(this).createProduct(productReq).enqueue(new Callback<ProductResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponseDto> call, @NonNull Response<ProductResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddProductActivity.this, "Product created successfully on server!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddProductActivity.this, "Failed to create product. Server error code: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponseDto> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AddProductActivity.this, "Connection error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSave != null) {
            btnSave.setEnabled(!isLoading);
        }
    }
}