package com.retail.seller.ui.products;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText etName, etSku, etCategory, etBrand, etDescription, etImageUrl;
    private LinearLayout layoutVariantsContainer;
    private MaterialButton btnAddVariant, btnSave;
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

        // Basic Info Fields
        etName = findViewById(R.id.et_product_name);
        etSku = findViewById(R.id.et_product_sku);
        etCategory = findViewById(R.id.et_category);
        etBrand = findViewById(R.id.et_brand);
        etDescription = findViewById(R.id.et_description);
        etImageUrl = findViewById(R.id.et_image_url);

        // Dynamic Variants Container
        layoutVariantsContainer = findViewById(R.id.layout_variants_container);
        btnAddVariant = findViewById(R.id.btn_add_variant);
        btnSave = findViewById(R.id.btn_save_product);
        progressBar = findViewById(R.id.add_product_progress);

        // Default ek variant UI add karna
        addVariantView();

        btnAddVariant.setOnClickListener(v -> addVariantView());
        btnSave.setOnClickListener(v -> attemptSaveProduct());
    }

    private void addVariantView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View variantView = inflater.inflate(R.layout.item_variant_input_form, layoutVariantsContainer, false);

        ImageButton btnRemove = variantView.findViewById(R.id.btn_remove_variant);
        btnRemove.setOnClickListener(v -> {
            if (layoutVariantsContainer.getChildCount() > 1) {
                layoutVariantsContainer.removeView(variantView);
            } else {
                Toast.makeText(this, "Product must have at least one variant!", Toast.LENGTH_SHORT).show();
            }
        });

        layoutVariantsContainer.addView(variantView);
    }

    private void attemptSaveProduct() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String sku = etSku.getText() != null ? etSku.getText().toString().trim() : "";
        String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";
        String brand = etBrand.getText() != null ? etBrand.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String imageUrl = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sku) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please fill all mandatory product details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Loop through all variant forms to extract data
        List<ProductVariantRequestDto> variantsList = new ArrayList<>();
        int variantCount = layoutVariantsContainer.getChildCount();

        for (int i = 0; i < variantCount; i++) {
            View view = layoutVariantsContainer.getChildAt(i);

            TextInputEditText etVarSku = view.findViewById(R.id.et_var_sku);
            TextInputEditText etVarSize = view.findViewById(R.id.et_var_size);
            TextInputEditText etVarColor = view.findViewById(R.id.et_var_color);
            TextInputEditText etVarSellPrice = view.findViewById(R.id.et_var_sell_price);
            TextInputEditText etVarStock = view.findViewById(R.id.et_var_stock);

            String vSku = etVarSku.getText() != null ? etVarSku.getText().toString().trim() : "";
            String vSize = etVarSize.getText() != null ? etVarSize.getText().toString().trim() : "";
            String vColor = etVarColor.getText() != null ? etVarColor.getText().toString().trim() : "";
            String vSellStr = etVarSellPrice.getText() != null ? etVarSellPrice.getText().toString().trim() : "";
            String vStockStr = etVarStock.getText() != null ? etVarStock.getText().toString().trim() : "";

            if (TextUtils.isEmpty(vSku) || TextUtils.isEmpty(vSellStr) || TextUtils.isEmpty(vStockStr)) {
                Toast.makeText(this, "Please fill SKU, Price and Stock for all variants", Toast.LENGTH_LONG).show();
                return;
            }

            double sellPrice = Double.parseDouble(vSellStr);
            int stock = Integer.parseInt(vStockStr);

            ProductVariantRequestDto variantReq = new ProductVariantRequestDto(
                    vSku, vSize, vColor, sellPrice, 0.0, stock
            );
            variantsList.add(variantReq);
        }

        Long defaultSellerId = 1L; // Isko actual logged-in user ki ID se map kar sakte hain
        ProductRequestDto productReq = new ProductRequestDto(
                defaultSellerId, sku, name, description, category, brand,
                variantsList,
                !imageUrl.isEmpty() ? Collections.singletonList(imageUrl) : Collections.emptyList()
        );

        setLoading(true);

        ApiClient.getApiService(this).createProduct(productReq).enqueue(new Callback<ProductResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponseDto> call, @NonNull Response<ProductResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddProductActivity.this, "Product and " + variantsList.size() + " variants saved!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddProductActivity.this, "Failed to save. Error code: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponseDto> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AddProductActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnSave != null) btnSave.setEnabled(!isLoading);
        if (btnAddVariant != null) btnAddVariant.setEnabled(!isLoading);
    }
}