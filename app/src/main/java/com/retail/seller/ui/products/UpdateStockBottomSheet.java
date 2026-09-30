package com.retail.seller.ui.products;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import com.retail.seller.data.model.StockUpdateRequestDto;
import com.retail.seller.data.network.ApiClient;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateStockBottomSheet extends BottomSheetDialogFragment {

    public interface OnStockUpdatedListener {
        void onStockUpdated();
    }

    private ProductResponseDto product;
    private OnStockUpdatedListener listener;
    private final Map<Long, Integer> updatedStockMap = new HashMap<>();

    private ProgressBar progressBar;
    private MaterialButton btnSave;

    public static UpdateStockBottomSheet newInstance(ProductResponseDto product, OnStockUpdatedListener listener) {
        UpdateStockBottomSheet sheet = new UpdateStockBottomSheet();
        sheet.product = product;
        sheet.listener = listener;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_update_stock_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_bs_product_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_bs_product_subtitle);
        RecyclerView rvVariants = view.findViewById(R.id.rv_bs_variants);
        btnSave = view.findViewById(R.id.btn_bs_save_stock);
        progressBar = view.findViewById(R.id.bs_stock_progress);

        if (product != null) {
            tvTitle.setText(product.getName() != null ? product.getName() : "Update Stock");
            tvSubtitle.setText(String.format(Locale.getDefault(), "SKU: %s | Brand: %s",
                    product.getProductCode() != null ? product.getProductCode() : "N/A",
                    product.getBrand() != null ? product.getBrand() : "Generic"));

            rvVariants.setLayoutManager(new LinearLayoutManager(requireContext()));
            VariantStockUpdateAdapter adapter = new VariantStockUpdateAdapter(product.getVariants(), updatedStockMap);
            rvVariants.setAdapter(adapter);
        }

        btnSave.setOnClickListener(v -> saveStockChanges());
    }

    private void saveStockChanges() {
        if (updatedStockMap.isEmpty()) {
            Toast.makeText(requireContext(), "No stock changes entered.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        setLoading(true);

        final int totalUpdates = updatedStockMap.size();
        final int[] completedUpdates = {0};
        final boolean[] hasErrors = {false};

        for (Map.Entry<Long, Integer> entry : updatedStockMap.entrySet()) {
            Long variantId = entry.getKey();
            Integer newStock = entry.getValue();

            StockUpdateRequestDto request = new StockUpdateRequestDto(newStock);

            ApiClient.getApiService(requireContext()).updateVariantStock(variantId, request).enqueue(new Callback<ProductVariantDto>() {
                @Override
                public void onResponse(@NonNull Call<ProductVariantDto> call, @NonNull Response<ProductVariantDto> response) {
                    completedUpdates[0]++;
                    if (!response.isSuccessful()) {
                        hasErrors[0] = true;
                    }
                    checkCompletion(completedUpdates[0], totalUpdates, hasErrors[0]);
                }

                @Override
                public void onFailure(@NonNull Call<ProductVariantDto> call, @NonNull Throwable t) {
                    completedUpdates[0]++;
                    hasErrors[0] = true;
                    checkCompletion(completedUpdates[0], totalUpdates, true);
                }
            });
        }
    }

    private void checkCompletion(int current, int total, boolean errorOccurred) {
        if (current >= total) {
            setLoading(false);
            if (!errorOccurred) {
                Toast.makeText(requireContext(), "Inventory stock updated successfully!", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onStockUpdated();
                }
                dismiss();
            } else {
                Toast.makeText(requireContext(), "Some stock updates failed. Please check connection.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (btnSave != null) btnSave.setEnabled(!isLoading);
    }

    // Adapter for Bottom Sheet variant stock input list
    private static class VariantStockUpdateAdapter extends RecyclerView.Adapter<VariantStockUpdateAdapter.Holder> {

        private final List<ProductVariantDto> variants;
        private final Map<Long, Integer> stockMap;

        public VariantStockUpdateAdapter(List<ProductVariantDto> variants, Map<Long, Integer> stockMap) {
            this.variants = variants;
            this.stockMap = stockMap;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_variant_stock_input, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ProductVariantDto variant = variants.get(position);

            holder.tvTitle.setText(String.format(Locale.getDefault(), "Size: %s | %s",
                    variant.getSize() != null && !variant.getSize().isEmpty() ? variant.getSize() : "Standard",
                    variant.getColor() != null && !variant.getColor().isEmpty() ? variant.getColor() : "Standard"));

            holder.tvSkuStock.setText(String.format(Locale.getDefault(), "SKU: %s | Current: %d",
                    variant.getSku() != null ? variant.getSku() : "N/A",
                    variant.getStockQuantity()));

            holder.etNewStock.setHint(String.valueOf(variant.getStockQuantity()));

            holder.etNewStock.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s != null && s.length() > 0) {
                        try {
                            int val = Integer.parseInt(s.toString().trim());
                            stockMap.put(variant.getId(), val);
                        } catch (NumberFormatException ignored) {
                            stockMap.remove(variant.getId());
                        }
                    } else {
                        stockMap.remove(variant.getId());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        @Override
        public int getItemCount() {
            return variants != null ? variants.size() : 0;
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvSkuStock;
            TextInputEditText etNewStock;

            public Holder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_bs_variant_title);
                tvSkuStock = itemView.findViewById(R.id.tv_bs_variant_sku_stock);
                etNewStock = itemView.findViewById(R.id.et_bs_new_stock);
            }
        }
    }
}