package com.retail.seller.ui.inventory;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import com.retail.seller.data.model.ReturnItemResponseDto;
import com.retail.seller.data.model.ReturnResponseDto;
import com.retail.seller.data.model.SaleItemDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.model.StockUpdateRequestDto;
import com.retail.seller.data.network.ApiClient;
import com.retail.seller.data.network.TokenManager;
import com.retail.seller.ui.returns.ReturnProcessActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private TextView tvAvailableUnits, tvLowStock, tvOutOfStock;
    private TextInputEditText etSearchInventory;
    private ChipGroup chipGroupFilter;
    private LinearLayout layoutListContainer;
    private ProgressBar progressBar;

    private final List<VariantInventoryItem> allVariantItems = new ArrayList<>();
    private final List<com.retail.seller.data.model.InventoryTransactionDto> auditLogs = new ArrayList<>();
    private int currentFilterChipId = R.id.chip_inv_all;

    public InventoryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipe_refresh_inventory);
        tvAvailableUnits = view.findViewById(R.id.tv_inv_available_units);
        tvLowStock = view.findViewById(R.id.tv_inv_low_stock);
        tvOutOfStock = view.findViewById(R.id.tv_inv_out_of_stock);
        etSearchInventory = view.findViewById(R.id.et_search_inventory);
        chipGroupFilter = view.findViewById(R.id.chip_group_inventory_filter);
        layoutListContainer = view.findViewById(R.id.layout_inventory_list_container);
        progressBar = view.findViewById(R.id.inventory_progress);

        MaterialButton btnReturn = view.findViewById(R.id.btn_inventory_return);
        btnReturn.setOnClickListener(v -> startActivity(new Intent(requireContext(), ReturnProcessActivity.class)));

        swipeRefresh.setOnRefreshListener(this::loadInventoryData);

        etSearchInventory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderCurrentView();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            currentFilterChipId = checkedIds.isEmpty() ? R.id.chip_inv_all : checkedIds.get(0);
            renderCurrentView();
        });

        loadInventoryData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInventoryData();
    }

    private void loadInventoryData() {
        if (!isAdded()) return;
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService(requireContext()).getMyProducts(0, 100).enqueue(new Callback<List<ProductResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductResponseDto>> call, @NonNull Response<List<ProductResponseDto>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);

                allVariantItems.clear();
                int totalUnits = 0;
                int lowStockCount = 0;
                int outStockCount = 0;

                if (response.isSuccessful() && response.body() != null) {
                    for (ProductResponseDto p : response.body()) {
                        if (p.getVariants() != null) {
                            for (ProductVariantDto v : p.getVariants()) {
                                allVariantItems.add(new VariantInventoryItem(p, v));
                                int st = v.getStockQuantity();
                                totalUnits += st;
                                if (st <= 0) {
                                    outStockCount++;
                                } else if (st < 10) {
                                    lowStockCount++;
                                }
                            }
                        }
                    }
                }

                tvAvailableUnits.setText(String.valueOf(totalUnits));
                tvLowStock.setText(String.valueOf(lowStockCount));
                tvOutOfStock.setText(String.valueOf(outStockCount));
                renderCurrentView();
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductResponseDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error loading inventory: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch Return Restock History & Order Stock Movement History
        ApiClient.getApiService(requireContext()).getInventoryTransactions(0, 50)
                .enqueue(new Callback<List<com.retail.seller.data.model.InventoryTransactionDto>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<List<com.retail.seller.data.model.InventoryTransactionDto>> call,
                            @NonNull Response<List<com.retail.seller.data.model.InventoryTransactionDto>> response) {
                        if (!isAdded()) return;
                        auditLogs.clear();
                        if (response.isSuccessful() && response.body() != null) {
                            auditLogs.addAll(response.body());
                        }
                        if (currentFilterChipId == R.id.chip_inv_history) {
                            renderCurrentView();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<com.retail.seller.data.model.InventoryTransactionDto>> call,
                            @NonNull Throwable t) {
                    }
                });
    }

    private void renderCurrentView() {
        if (layoutListContainer == null || getContext() == null) return;
        layoutListContainer.removeAllViews();

        if (currentFilterChipId == R.id.chip_inv_history) {
            renderTransactionHistory();
            return;
        }

        String query = etSearchInventory != null && etSearchInventory.getText() != null
                ? etSearchInventory.getText().toString().trim().toLowerCase(Locale.ROOT) : "";

        float density = getResources().getDisplayMetrics().density;
        int count = 0;

        for (VariantInventoryItem item : allVariantItems) {
            int stock = item.variant.getStockQuantity();
            if (currentFilterChipId == R.id.chip_inv_available && stock <= 0) continue;
            if (currentFilterChipId == R.id.chip_inv_low && (stock <= 0 || stock >= 10)) continue;
            if (currentFilterChipId == R.id.chip_inv_out && stock > 0) continue;

            if (!query.isEmpty()) {
                String searchable = ((item.product.getName() != null ? item.product.getName() : "") + " "
                        + (item.variant.getSku() != null ? item.variant.getSku() : "") + " "
                        + (item.variant.getSize() != null ? item.variant.getSize() : "") + " "
                        + (item.variant.getColor() != null ? item.variant.getColor() : "")).toLowerCase(Locale.ROOT);
                if (!searchable.contains(query)) continue;
            }

            MaterialCardView card = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = (int) (10 * density);
            card.setLayoutParams(cardParams);
            card.setRadius(14 * density);
            card.setCardElevation(2 * density);
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_white));

            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (14 * density);
            inner.setPadding(pad, pad, pad, pad);

            // Top row: Product Name + Stock Status Badge
            LinearLayout topRow = new LinearLayout(requireContext());
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvTitle = new TextView(requireContext());
            tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            tvTitle.setText(item.product.getName());
            tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvTitle.setTextSize(15f);
            tvTitle.setTypeface(null, Typeface.BOLD);
            topRow.addView(tvTitle);

            TextView tvBadge = new TextView(requireContext());
            tvBadge.setPadding((int) (10 * density), (int) (4 * density), (int) (10 * density), (int) (4 * density));
            tvBadge.setTextSize(11.5f);
            tvBadge.setTypeface(null, Typeface.BOLD);
            if (stock <= 0) {
                tvBadge.setText("OUT OF STOCK (0)");
                tvBadge.setBackgroundResource(R.drawable.bg_badge_red);
                tvBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_red));
            } else if (stock < 10) {
                tvBadge.setText("LOW STOCK: " + stock);
                tvBadge.setBackgroundResource(R.drawable.bg_badge_amber);
                tvBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.amber_warning_dark));
            } else {
                tvBadge.setText("IN STOCK: " + stock);
                tvBadge.setBackgroundResource(R.drawable.bg_badge_emerald);
                tvBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.emerald_dark));
            }
            topRow.addView(tvBadge);
            inner.addView(topRow);

            // Middle row: SKU, Size, Color, Wholesale Price, MOQ/Pack
            TextView tvMeta = new TextView(requireContext());
            String size = item.variant.getSize() != null && !item.variant.getSize().isEmpty() ? item.variant.getSize() : "Std";
            String color = item.variant.getColor() != null && !item.variant.getColor().isEmpty() ? item.variant.getColor() : "Std";
            tvMeta.setText(String.format(Locale.getDefault(),
                    "SKU: %s (ID: #%d) • Size: %s • Color: %s\nWholesale: ₹%.2f | Cost: ₹%.2f | MOQ: %d (Pack of %d)",
                    item.variant.getSku(),
                    item.variant.getId(),
                    size,
                    color,
                    item.variant.getSellingPrice(),
                    item.variant.getPurchasePrice(),
                    item.variant.getMinimumOrderQuantity(),
                    item.variant.getPackSize()));
            tvMeta.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvMeta.setTextSize(12.5f);
            tvMeta.setPadding(0, (int) (6 * density), 0, (int) (8 * density));
            inner.addView(tvMeta);

            // Bottom row: Quick Stock Adjustment Button
            MaterialButton btnAdjust = new MaterialButton(requireContext());
            btnAdjust.setText("Adjust Stock Quantity");
            btnAdjust.setAllCaps(false);
            btnAdjust.setTextSize(12.5f);
            btnAdjust.setOnClickListener(v -> showQuickAdjustStockDialog(item));
            inner.addView(btnAdjust);

            card.addView(inner);
            layoutListContainer.addView(card);
            count++;
        }

        if (count == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText("No product variants match the selected stock filter.");
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(24, 48, 24, 48);
            layoutListContainer.addView(empty);
        }
    }

    private void renderTransactionHistory() {
        float density = getResources().getDisplayMetrics().density;
        if (auditLogs.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No inventory movement records found yet.");
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(24, 48, 24, 48);
            layoutListContainer.addView(empty);
            return;
        }

        for (com.retail.seller.data.model.InventoryTransactionDto tx : auditLogs) {
            MaterialCardView card = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = (int) (8 * density);
            card.setLayoutParams(params);
            card.setRadius(12 * density);
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_white));

            LinearLayout box = new LinearLayout(requireContext());
            box.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (14 * density);
            box.setPadding(pad, pad, pad, pad);

            int delta = tx.getNewStock() - tx.getPreviousStock();
            String sign = delta >= 0 ? "+" : "";

            TextView tvHeader = new TextView(requireContext());
            tvHeader.setText(String.format(Locale.getDefault(), "%s (%s%d units) • %s #%s",
                    tx.getType(),
                    sign,
                    delta,
                    tx.getReferenceType(),
                    tx.getReferenceId() != null ? tx.getReferenceId() : "N/A"));
            tvHeader.setTextColor(ContextCompat.getColor(requireContext(),
                    delta >= 0 ? R.color.emerald_dark : R.color.royal_secondary));
            tvHeader.setTypeface(null, Typeface.BOLD);
            tvHeader.setTextSize(14f);
            box.addView(tvHeader);

            String dateStr = tx.getCreatedAt() != null ? tx.getCreatedAt().replace("T", " ") : "Recent";
            if (dateStr.length() > 16) dateStr = dateStr.substring(0, 16);

            TextView tvSub = new TextView(requireContext());
            tvSub.setText(String.format(Locale.getDefault(),
                    "%s (SKU: %s) • Stock: %d → %d\nDate: %s",
                    tx.getProductName(),
                    tx.getSku(),
                    tx.getPreviousStock(),
                    tx.getNewStock(),
                    dateStr));
            tvSub.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvSub.setTextSize(12f);
            box.addView(tvSub);

            card.addView(box);
            layoutListContainer.addView(card);
        }
    }

    private void showQuickAdjustStockDialog(VariantInventoryItem item) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.variant.getStockQuantity()));
        input.setSelection(input.getText().length());

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(pad, pad / 2, pad, 0);
        container.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle("Adjust Stock: " + item.variant.getSku())
                .setMessage(item.product.getName() + " (" + item.variant.getSize() + " | " + item.variant.getColor() + ")\nEnter updated total available stock:")
                .setView(container)
                .setPositiveButton("Save Stock", (d, w) -> {
                    String valStr = input.getText().toString().trim();
                    if (valStr.isEmpty()) return;
                    try {
                        int newStock = Integer.parseInt(valStr);
                        if (newStock < 0) {
                            Toast.makeText(requireContext(), "Stock cannot be negative.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        ApiClient.getApiService(requireContext())
                                .updateVariantStock(item.variant.getId(), new StockUpdateRequestDto(newStock))
                                .enqueue(new Callback<ProductVariantDto>() {
                                    @Override
                                    public void onResponse(@NonNull Call<ProductVariantDto> call, @NonNull Response<ProductVariantDto> response) {
                                        if (!isAdded()) return;
                                        progressBar.setVisibility(View.GONE);
                                        if (response.isSuccessful()) {
                                            Toast.makeText(requireContext(), "Stock updated to " + newStock + " units!", Toast.LENGTH_SHORT).show();
                                            loadInventoryData();
                                        } else {
                                            Toast.makeText(requireContext(),
                                                    ApiClient.extractErrorMessage(response, "Failed to update stock"),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<ProductVariantDto> call, @NonNull Throwable t) {
                                        if (!isAdded()) return;
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(requireContext(), "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (NumberFormatException ex) {
                        Toast.makeText(requireContext(), "Invalid stock quantity.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class VariantInventoryItem {
        final ProductResponseDto product;
        final ProductVariantDto variant;

        VariantInventoryItem(ProductResponseDto product, ProductVariantDto variant) {
            this.product = product;
            this.variant = variant;
        }
    }
}