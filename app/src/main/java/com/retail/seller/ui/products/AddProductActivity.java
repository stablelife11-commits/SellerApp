package com.retail.seller.ui.products;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductRequestDto;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import com.retail.seller.data.model.ProductVariantRequestDto;
import com.retail.seller.data.network.ApiClient;
import com.retail.seller.data.network.TokenManager;
import com.yalantis.ucrop.UCrop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_PRODUCT_ID = "EXTRA_EDIT_PRODUCT_ID";

    // TODO: Replace these 2 values with your Cloudinary Cloud Name and Unsigned Upload Preset
    private static final String CLOUDINARY_CLOUD_NAME = com.retail.seller.BuildConfig.CLOUDINARY_CLOUD_NAME;
    private static final String CLOUDINARY_UPLOAD_PRESET = com.retail.seller.BuildConfig.CLOUDINARY_UPLOAD_PRESET;

    private TextInputEditText etName, etSku, etCategory, etBrand, etDescription, etImageUrl;
    private ImageView ivProductPreview;
    private MaterialButton btnPickImage, btnAddVariant, btnSave;
    private ProgressBar imageUploadProgress, progressBar;
    private TextView tvUploadStatus;
    private LinearLayout layoutVariantsContainer;
    private MaterialToolbar toolbar;

    private Long editingProductId = null;
    private boolean isImageUploading = false;

    private final OkHttpClient cloudUploadClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // Step 1: Launch Gallery Picker -> Send selected Uri to uCrop
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), sourceUri -> {
                if (sourceUri != null) {
                    startPhotoCrop(sourceUri);
                }
            });

    // Step 2: Receive Cropped Image from uCrop -> Preview & Upload to Cloudinary
    private final ActivityResultLauncher<Intent> cropImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri croppedUri = UCrop.getOutput(result.getData());
                    if (croppedUri != null) {
                        ivProductPreview.setVisibility(View.VISIBLE);
                        ivProductPreview.setImageURI(null);
                        ivProductPreview.setImageURI(croppedUri);
                        uploadImageToCloudinary(croppedUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                    Throwable cropError = UCrop.getError(result.getData());
                    String msg = cropError != null ? cropError.getMessage() : "Unknown crop error";
                    setImageUploadError("Crop error: " + msg);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        toolbar = findViewById(R.id.toolbar_add_product);
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

        ivProductPreview = findViewById(R.id.iv_product_preview);
        btnPickImage = findViewById(R.id.btn_pick_image);
        imageUploadProgress = findViewById(R.id.image_upload_progress);
        tvUploadStatus = findViewById(R.id.tv_upload_status);

        layoutVariantsContainer = findViewById(R.id.layout_variants_container);
        btnAddVariant = findViewById(R.id.btn_add_variant);
        btnSave = findViewById(R.id.btn_save_product);
        progressBar = findViewById(R.id.add_product_progress);

        btnPickImage.setOnClickListener(v -> {
            if ("YOUR_CLOUD_NAME".equals(CLOUDINARY_CLOUD_NAME)) {
                Toast.makeText(this,
                        "Please set CLOUDINARY_CLOUD_NAME and CLOUDINARY_UPLOAD_PRESET in AddProductActivity.java first!",
                        Toast.LENGTH_LONG).show();
                return;
            }
            pickImageLauncher.launch("image/*");
        });

        btnAddVariant.setOnClickListener(v -> addVariantView(null));
        btnSave.setOnClickListener(v -> attemptSaveProduct());

        if (getIntent() != null && getIntent().hasExtra(EXTRA_EDIT_PRODUCT_ID)) {
            long id = getIntent().getLongExtra(EXTRA_EDIT_PRODUCT_ID, -1L);
            if (id > 0) {
                editingProductId = id;
                toolbar.setTitle("Edit Product #" + id);
                btnSave.setText("Update Product");
                loadExistingProductForEdit(id);
                return;
            }
        }

        addVariantView(null);
    }

    private void startPhotoCrop(Uri sourceUri) {
        String destFileName = "cropped_product_" + System.currentTimeMillis() + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destFileName));

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(88);
        options.setToolbarTitle("Crop Product Photo");
        options.setToolbarColor(ContextCompat.getColor(this, R.color.indigo_primary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.indigo_dark));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.indigo_primary));
        options.setFreeStyleCropEnabled(true);

        Intent cropIntent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1080, 1080)
                .withOptions(options)
                .getIntent(this);

        cropImageLauncher.launch(cropIntent);
    }

    private void uploadImageToCloudinary(Uri croppedImageUri) {
        setImageUploadLoading(true, "Uploading cropped photo to cloud…");

        new Thread(() -> {
            try {
                byte[] compressedJpeg = compressUriToJpegBytes(croppedImageUri);
                if (compressedJpeg == null || compressedJpeg.length == 0) {
                    runOnUiThread(() -> setImageUploadError("Could not read cropped image file."));
                    return;
                }

                String uploadUrl = "https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/image/upload";

                RequestBody fileBody = RequestBody.create(compressedJpeg, MediaType.parse("image/jpeg"));
                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                        .addFormDataPart("folder", "retail_flow_products")
                        .addFormDataPart("file", "product_" + System.currentTimeMillis() + ".jpg", fileBody)
                        .build();

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                cloudUploadClient.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        runOnUiThread(() -> setImageUploadError("Cloud upload failed: " + e.getLocalizedMessage()));
                    }

                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                        String bodyStr = response.body() != null ? response.body().string() : "";
                        if (response.isSuccessful() && !bodyStr.isEmpty()) {
                            try {
                                JsonObject json = JsonParser.parseString(bodyStr).getAsJsonObject();
                                String secureUrl = json.has("secure_url")
                                        ? json.get("secure_url").getAsString()
                                        : json.get("url").getAsString();

                                runOnUiThread(() -> {
                                    setImageUploadLoading(false, "✓ Cropped photo uploaded to cloud!");
                                    tvUploadStatus.setTextColor(ContextCompat.getColor(AddProductActivity.this, R.color.emerald_dark));
                                    etImageUrl.setText(secureUrl);
                                    btnPickImage.setText("📷 Change / Re-crop Product Photo");
                                    Toast.makeText(AddProductActivity.this, "Photo uploaded to cloud!", Toast.LENGTH_SHORT).show();
                                });
                            } catch (Exception ex) {
                                runOnUiThread(() -> setImageUploadError("Error parsing cloud response."));
                            }
                        } else {
                            runOnUiThread(() -> setImageUploadError("Cloudinary error (" + response.code() + "): Check preset name & Unsigned mode."));
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> setImageUploadError("Error processing image: " + e.getLocalizedMessage()));
            }
        }).start();
    }

    private byte[] compressUriToJpegBytes(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            if (original == null) return null;

            int maxDim = 1080;
            int width = original.getWidth();
            int height = original.getHeight();
            if (width > maxDim || height > maxDim) {
                float ratio = (float) width / (float) height;
                if (ratio > 1) {
                    width = maxDim;
                    height = Math.round(width / ratio);
                } else {
                    height = maxDim;
                    width = Math.round(height * ratio);
                }
                original = Bitmap.createScaledBitmap(original, width, height, true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            return baos.toByteArray();
        }
    }

    private void setImageUploadLoading(boolean uploading, String statusText) {
        isImageUploading = uploading;
        if (imageUploadProgress != null) {
            imageUploadProgress.setVisibility(uploading ? View.VISIBLE : View.GONE);
        }
        if (btnPickImage != null) {
            btnPickImage.setEnabled(!uploading);
        }
        if (btnSave != null) {
            btnSave.setEnabled(!uploading);
        }
        if (tvUploadStatus != null) {
            tvUploadStatus.setText(statusText);
            tvUploadStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void setImageUploadError(String errorMsg) {
        setImageUploadLoading(false, errorMsg);
        if (tvUploadStatus != null) {
            tvUploadStatus.setTextColor(ContextCompat.getColor(this, R.color.warning_red));
        }
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }

    private void loadExistingProductForEdit(Long productId) {
        setLoading(true);
        ApiClient.getApiService(this).getProductById(productId).enqueue(new Callback<ProductResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponseDto> call, @NonNull Response<ProductResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    populateProductForm(response.body());
                } else {
                    String msg = ApiClient.extractErrorMessage(response, "Could not load product details");
                    Toast.makeText(AddProductActivity.this, msg, Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponseDto> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AddProductActivity.this, "Network error loading product: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void populateProductForm(ProductResponseDto product) {
        etName.setText(product.getName());
        etSku.setText(product.getProductCode());
        etCategory.setText(product.getCategory());
        etBrand.setText(product.getBrand());
        etDescription.setText(product.getDescription());

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            String existingUrl = product.getImageUrls().get(0);
            etImageUrl.setText(existingUrl);
            btnPickImage.setText("📷 Change / Replace Product Photo");
            tvUploadStatus.setText("Current cloud photo loaded. Tap button above to crop & replace.");
            ivProductPreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(existingUrl)
                    .placeholder(R.drawable.ic_box)
                    .error(R.drawable.ic_box)
                    .centerCrop()
                    .into(ivProductPreview);
        }

        layoutVariantsContainer.removeAllViews();
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (ProductVariantDto variant : product.getVariants()) {
                addVariantView(variant);
            }
        } else {
            addVariantView(null);
        }
    }

    private void addVariantView(ProductVariantDto existingVariant) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View variantView = inflater.inflate(R.layout.item_variant_input_form, layoutVariantsContainer, false);

        if (existingVariant != null) {
            variantView.setTag(existingVariant.getId());
            TextInputEditText etVarSku = variantView.findViewById(R.id.et_var_sku);
            TextInputEditText etVarSize = variantView.findViewById(R.id.et_var_size);
            TextInputEditText etVarColor = variantView.findViewById(R.id.et_var_color);
            TextInputEditText etVarSellPrice = variantView.findViewById(R.id.et_var_sell_price);
            TextInputEditText etVarPurchasePrice = variantView.findViewById(R.id.et_var_purchase_price);
            TextInputEditText etVarStock = variantView.findViewById(R.id.et_var_stock);
            TextInputEditText etVarMoq = variantView.findViewById(R.id.et_var_moq);
            TextInputEditText etVarPackSize = variantView.findViewById(R.id.et_var_pack_size);
            TextInputEditText etVarHsn = variantView.findViewById(R.id.et_var_hsn);
            TextInputEditText etVarGst = variantView.findViewById(R.id.et_var_gst);

            etVarSku.setText(existingVariant.getSku());
            etVarSize.setText(existingVariant.getSize());
            etVarColor.setText(existingVariant.getColor());
            etVarSellPrice.setText(String.valueOf(existingVariant.getSellingPrice()));
            etVarPurchasePrice.setText(String.valueOf(existingVariant.getPurchasePrice()));
            etVarStock.setText(String.valueOf(existingVariant.getStock()));
            etVarMoq.setText(String.valueOf(existingVariant.getMinimumOrderQuantity()));
            etVarPackSize.setText(String.valueOf(existingVariant.getPackSize()));
            if (existingVariant.getHsnCode() != null) {
                etVarHsn.setText(existingVariant.getHsnCode());
            }
            etVarGst.setText(String.valueOf(existingVariant.getGstPercentage()));
        }

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
        if (isImageUploading) {
            Toast.makeText(this, "Please wait for the photo upload to finish.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = getTextSafe(etName);
        String productCode = getTextSafe(etSku);
        String category = getTextSafe(etCategory);
        String brand = getTextSafe(etBrand);
        String description = getTextSafe(etDescription);
        String imageUrl = getTextSafe(etImageUrl);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(productCode) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Product Name, Product Code, and Category are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ProductVariantRequestDto> variantsList = new ArrayList<>();
        int variantCount = layoutVariantsContainer.getChildCount();

        for (int i = 0; i < variantCount; i++) {
            View view = layoutVariantsContainer.getChildAt(i);
            Long existingVariantId = (view.getTag() instanceof Long) ? (Long) view.getTag() : null;

            TextInputEditText etVarSku = view.findViewById(R.id.et_var_sku);
            TextInputEditText etVarSize = view.findViewById(R.id.et_var_size);
            TextInputEditText etVarColor = view.findViewById(R.id.et_var_color);
            TextInputEditText etVarSellPrice = view.findViewById(R.id.et_var_sell_price);
            TextInputEditText etVarPurchasePrice = view.findViewById(R.id.et_var_purchase_price);
            TextInputEditText etVarStock = view.findViewById(R.id.et_var_stock);
            TextInputEditText etVarMoq = view.findViewById(R.id.et_var_moq);
            TextInputEditText etVarPackSize = view.findViewById(R.id.et_var_pack_size);
            TextInputEditText etVarHsn = view.findViewById(R.id.et_var_hsn);
            TextInputEditText etVarGst = view.findViewById(R.id.et_var_gst);

            String vSku = getTextSafe(etVarSku);
            String vSize = getTextSafe(etVarSize);
            String vColor = getTextSafe(etVarColor);
            String vSellStr = getTextSafe(etVarSellPrice);
            String vPurchaseStr = getTextSafe(etVarPurchasePrice);
            String vStockStr = getTextSafe(etVarStock);
            String vMoqStr = getTextSafe(etVarMoq);
            String vPackStr = getTextSafe(etVarPackSize);
            String vHsn = getTextSafe(etVarHsn);
            String vGstStr = getTextSafe(etVarGst);

            if (TextUtils.isEmpty(vSku) || TextUtils.isEmpty(vSellStr) || TextUtils.isEmpty(vStockStr)) {
                Toast.makeText(this, "Variant #" + (i + 1) + ": SKU, Selling Price, and Stock are required.", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                double sellPrice = Double.parseDouble(vSellStr);
                double purchasePrice = TextUtils.isEmpty(vPurchaseStr) ? 0.0 : Double.parseDouble(vPurchaseStr);
                int stock = Integer.parseInt(vStockStr);
                int moq = TextUtils.isEmpty(vMoqStr) ? 1 : Integer.parseInt(vMoqStr);
                int packSize = TextUtils.isEmpty(vPackStr) ? 1 : Integer.parseInt(vPackStr);
                double gst = TextUtils.isEmpty(vGstStr) ? 0.0 : Double.parseDouble(vGstStr);

                if (sellPrice <= 0) {
                    Toast.makeText(this, "Selling price must be greater than 0 for SKU: " + vSku, Toast.LENGTH_LONG).show();
                    return;
                }
                if (moq < 1 || packSize < 1 || moq % packSize != 0) {
                    Toast.makeText(this, "MOQ (" + moq + ") must be a multiple of Pack Size (" + packSize + ") for SKU: " + vSku, Toast.LENGTH_LONG).show();
                    return;
                }

                ProductVariantRequestDto variantReq = new ProductVariantRequestDto(
                        existingVariantId,
                        vSku,
                        vSize,
                        vColor,
                        sellPrice,
                        purchasePrice,
                        stock,
                        moq,
                        packSize,
                        TextUtils.isEmpty(vHsn) ? null : vHsn,
                        gst
                );
                variantsList.add(variantReq);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Invalid number format in Variant #" + (i + 1), Toast.LENGTH_LONG).show();
                return;
            }
        }

        Long sellerId = TokenManager.getInstance(this).getUserId();
        if (sellerId == null) sellerId = 1L;

        ProductRequestDto productReq = new ProductRequestDto(
                sellerId,
                productCode,
                name,
                description,
                category,
                brand,
                variantsList,
                !imageUrl.isEmpty() ? Collections.singletonList(imageUrl) : Collections.emptyList()
        );

        setLoading(true);

        Call<ProductResponseDto> apiCall = (editingProductId != null)
                ? ApiClient.getApiService(this).updateProduct(editingProductId, productReq)
                : ApiClient.getApiService(this).createProduct(productReq);

        apiCall.enqueue(new Callback<ProductResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponseDto> call, @NonNull Response<ProductResponseDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    String action = editingProductId != null ? "updated" : "created";
                    Toast.makeText(AddProductActivity.this, "Product " + action + " successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = ApiClient.extractErrorMessage(response, "Failed to save product");
                    Toast.makeText(AddProductActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponseDto> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(AddProductActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getTextSafe(TextInputEditText editText) {
        return editText != null && editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnSave != null) btnSave.setEnabled(!isLoading && !isImageUploading);
        if (btnAddVariant != null) btnAddVariant.setEnabled(!isLoading);
    }
}