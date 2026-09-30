package com.retail.seller.ui.products;

import android.content.Intent;
import android.os.Bundle;
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
import com.retail.seller.R;
import com.retail.seller.data.model.ProductResponseDto;
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
    private ProductAdapter adapter;

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
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_product);

        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductAdapter(requireContext(), new ArrayList<>(), this::openUpdateStockBottomSheet);
        rvProducts.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchProducts);

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

    @Override
    public void onResume() {
        super.onResume();
        fetchProducts();
    }

    private void fetchProducts() {
        if (!swipeRefresh.isRefreshing() && productsProgress != null) {
            productsProgress.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService(requireContext()).getProducts().enqueue(new Callback<List<ProductResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductResponseDto>> call, @NonNull Response<List<ProductResponseDto>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    displayProducts(response.body());
                } else {
                    displayProducts(Collections.emptyList());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading products from server (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductResponseDto>> call, @NonNull Throwable t) {
                hideLoading();
                displayProducts(Collections.emptyList());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to connect: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void hideLoading() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        if (productsProgress != null) productsProgress.setVisibility(View.GONE);
    }

    private void displayProducts(List<ProductResponseDto> products) {
        if (products == null || products.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
            tvProductCountSummary.setText(String.format(Locale.getDefault(), "%d item(s)", 0));
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
            adapter.updateProducts(products);
            tvProductCountSummary.setText(String.format(Locale.getDefault(), "%d item(s)", products.size()));
        }
    }
}