package com.retail.seller.ui.products;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import com.retail.seller.data.network.ApiClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvProducts;
    private LinearLayout layoutEmpty;
    private ProgressBar productsProgress;
    private TextView tvProductCountSummary;
    private TextInputEditText etSearchProducts;
    private ProductAdapter adapter;
    private final List<ProductResponseDto> allSellerProducts = new ArrayList<>();

    public ProductsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipe_refresh_products);
        rvProducts = view.findViewById(R.id.rv_products);
        layoutEmpty = view.findViewById(R.id.layout_empty_products);
        productsProgress = view.findViewById(R.id.products_progress);
        tvProductCountSummary = view.findViewById(R.id.tv_product_count_summary);
        etSearchProducts = view.findViewById(R.id.et_search_products);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_product);

        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductAdapter(requireContext(), new ArrayList<>(), new ProductAdapter.OnProductActionListener() {
            @Override
            public void onUpdateStockClick(ProductResponseDto product) {
                openUpdateStockBottomSheet(product);
            }

            @Override
            public void onEditProductClick(ProductResponseDto product) {
                Intent intent = new Intent(requireContext(), AddProductActivity.class);
                intent.putExtra(AddProductActivity.EXTRA_EDIT_PRODUCT_ID, product.getId());
                startActivity(intent);
            }

            @Override
            public void onToggleStatusClick(ProductResponseDto product) {
                confirmAndToggleStatus(product);
            }
        });
        rvProducts.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchProducts);

        etSearchProducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddProductActivity.class);
            startActivity(intent);
        });

        fetchProducts();
    }

    private void openUpdateStockBottomSheet(ProductResponseDto product) {
        if (product != null && product.getVariants() != null && !product.getVariants().isEmpty()) {
            UpdateStockBottomSheet.newInstance(product, this::fetchProducts)
                    .show(getChildFragmentManager(), "UpdateStockBottomSheet");
        } else {
            Toast.makeText(requireContext(), "No variants available for this product.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmAndToggleStatus(ProductResponseDto product) {
        boolean currentlyActive = Boolean.TRUE.equals(product.getActive());
        boolean targetStatus = !currentlyActive;
        String actionName = targetStatus ? "Activate" : "Deactivate";

        new AlertDialog.Builder(requireContext())
                .setTitle(actionName + " Product")
                .setMessage("Are you sure you want to " + actionName.toLowerCase(Locale.ROOT) + " '" + product.getName() + "'?")
                .setPositiveButton(actionName, (dialog, which) -> {
                    productsProgress.setVisibility(View.VISIBLE);
                    ApiClient.getApiService(requireContext())
                            .updateProductStatus(product.getId(), targetStatus)
                            .enqueue(new Callback<ProductResponseDto>() {
                                @Override
                                public void onResponse(@NonNull Call<ProductResponseDto> call, @NonNull Response<ProductResponseDto> response) {
                                    if (!isAdded()) return;
                                    productsProgress.setVisibility(View.GONE);
                                    if (response.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Product status updated!", Toast.LENGTH_SHORT).show();
                                        fetchProducts();
                                    } else {
                                        String err = ApiClient.extractErrorMessage(response, "Failed to update status");
                                        Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ProductResponseDto> call, @NonNull Throwable t) {
                                    if (!isAdded()) return;
                                    productsProgress.setVisibility(View.GONE);
                                    Toast.makeText(requireContext(), "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchProducts();
    }

    private void fetchProducts() {
        if (!isAdded()) return;
        if (!swipeRefresh.isRefreshing() && productsProgress != null) {
            productsProgress.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService(requireContext()).getMyProducts(0, 100).enqueue(new Callback<List<ProductResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductResponseDto>> call, @NonNull Response<List<ProductResponseDto>> response) {
                if (!isAdded()) return;
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    allSellerProducts.clear();
                    allSellerProducts.addAll(response.body());
                    String currentQuery = etSearchProducts != null && etSearchProducts.getText() != null
                            ? etSearchProducts.getText().toString() : "";
                    filterProducts(currentQuery);
                } else {
                    allSellerProducts.clear();
                    displayProducts(Collections.emptyList());
                    String err = ApiClient.extractErrorMessage(response, "Error loading seller catalog");
                    Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductResponseDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                hideLoading();
                displayProducts(Collections.emptyList());
                Toast.makeText(requireContext(), "Failed to connect: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            displayProducts(allSellerProducts);
            return;
        }
        String lower = query.trim().toLowerCase(Locale.ROOT);
        List<ProductResponseDto> filtered = new ArrayList<>();
        for (ProductResponseDto p : allSellerProducts) {
            boolean matchesProduct = (p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(lower))
                    || (p.getProductCode() != null && p.getProductCode().toLowerCase(Locale.ROOT).contains(lower))
                    || (p.getCategory() != null && p.getCategory().toLowerCase(Locale.ROOT).contains(lower))
                    || (p.getBrand() != null && p.getBrand().toLowerCase(Locale.ROOT).contains(lower));

            boolean matchesVariantSku = false;
            if (p.getVariants() != null) {
                for (ProductVariantDto v : p.getVariants()) {
                    if (v.getSku() != null && v.getSku().toLowerCase(Locale.ROOT).contains(lower)) {
                        matchesVariantSku = true;
                        break;
                    }
                }
            }

            if (matchesProduct || matchesVariantSku) {
                filtered.add(p);
            }
        }
        displayProducts(filtered);
    }

    private void hideLoading() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        if (productsProgress != null) productsProgress.setVisibility(View.GONE);
    }

    private void displayProducts(List<ProductResponseDto> products) {
        if (products == null || products.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
            tvProductCountSummary.setText(String.format(Locale.getDefault(), "%d product(s) in your catalog", 0));
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
            adapter.updateProducts(products);
            tvProductCountSummary.setText(String.format(Locale.getDefault(), "%d product(s) in your catalog", products.size()));
        }
    }
}