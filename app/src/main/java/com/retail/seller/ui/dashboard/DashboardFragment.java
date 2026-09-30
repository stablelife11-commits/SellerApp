package com.retail.seller.ui.dashboard;

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
import com.google.android.material.button.MaterialButton;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.network.ApiClient;
import com.retail.seller.data.network.TokenManager;
import com.retail.seller.ui.auth.LoginActivity;
import com.retail.seller.ui.products.AddProductActivity;
import com.retail.seller.ui.returns.ReturnProcessActivity;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TextView tvActiveProductsCount;
    private TextView tvLowStockCount;
    private TextView tvTotalRevenue;
    private TextView tvWelcomeTitle;
    private TextView tvUserEmail;
    private LinearLayout layoutRecentSalesContainer;
    private ProgressBar dashboardProgress;

    public DashboardFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcomeTitle = view.findViewById(R.id.tv_welcome_title);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvActiveProductsCount = view.findViewById(R.id.tv_active_products_count);
        tvLowStockCount = view.findViewById(R.id.tv_low_stock_count);
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        layoutRecentSalesContainer = view.findViewById(R.id.layout_recent_sales_container);
        dashboardProgress = view.findViewById(R.id.dashboard_progress);

        MaterialButton btnAddProduct = view.findViewById(R.id.btn_action_add_product);
        MaterialButton btnProcessReturn = view.findViewById(R.id.btn_action_process_return);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);

        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        if (tokenManager.getName() != null && !tokenManager.getName().isEmpty()) {
            tvWelcomeTitle.setText("Welcome, " + tokenManager.getName() + "!");
        }
        if (tokenManager.getEmail() != null && !tokenManager.getEmail().isEmpty()) {
            tvUserEmail.setText(tokenManager.getEmail());
        }

        btnLogout.setOnClickListener(v -> {
            tokenManager.clearSession();
            Toast.makeText(requireContext(), "Signed out successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddProductActivity.class);
            startActivity(intent);
        });

        btnProcessReturn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReturnProcessActivity.class);
            startActivity(intent);
        });

        loadDashboardData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (dashboardProgress != null) dashboardProgress.setVisibility(View.VISIBLE);

        // Fetch products for stats calculation directly from live backend
        ApiClient.getApiService(requireContext()).getProducts().enqueue(new Callback<List<ProductResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductResponseDto>> call, @NonNull Response<List<ProductResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateProductStats(response.body());
                } else {
                    updateProductStats(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductResponseDto>> call, @NonNull Throwable t) {
                updateProductStats(Collections.emptyList());
            }
        });

        // Fetch sales for revenue and recent sales list directly from live backend
        ApiClient.getApiService(requireContext()).getSales().enqueue(new Callback<List<SaleResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<SaleResponseDto>> call, @NonNull Response<List<SaleResponseDto>> response) {
                if (dashboardProgress != null) dashboardProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    updateSalesStatsAndList(response.body());
                } else {
                    updateSalesStatsAndList(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SaleResponseDto>> call, @NonNull Throwable t) {
                if (dashboardProgress != null) dashboardProgress.setVisibility(View.GONE);
                updateSalesStatsAndList(Collections.emptyList());
            }
        });
    }

    private void updateProductStats(List<ProductResponseDto> products) {
        if (products == null) return;
        int activeProducts = products.size();
        int lowStockCount = 0;

        for (ProductResponseDto product : products) {
            if (product.getVariants() != null) {
                for (ProductVariantDto variant : product.getVariants()) {
                    if (variant.getStock() < 10) {
                        lowStockCount++;
                    }
                }
            }
        }

        if (tvActiveProductsCount != null) {
            tvActiveProductsCount.setText(String.valueOf(activeProducts));
        }
        if (tvLowStockCount != null) {
            tvLowStockCount.setText(String.valueOf(lowStockCount));
        }
    }

    private void updateSalesStatsAndList(List<SaleResponseDto> sales) {
        if (sales == null || getContext() == null) return;

        double totalRevenue = 0.0;
        for (SaleResponseDto sale : sales) {
            totalRevenue += sale.getTotalAmount();
        }

        if (tvTotalRevenue != null) {
            tvTotalRevenue.setText(String.format(Locale.getDefault(), "$%.2f", totalRevenue));
        }

        layoutRecentSalesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        int count = 0;
        for (SaleResponseDto sale : sales) {
            if (count >= 3) break; // Display top 3 recent sales

            View saleCardView = inflater.inflate(R.layout.item_sale_card, layoutRecentSalesContainer, false);
            TextView tvSaleNumber = saleCardView.findViewById(R.id.tv_sale_number);
            TextView tvSaleCustomer = saleCardView.findViewById(R.id.tv_sale_customer);
            TextView tvSaleTotalAmount = saleCardView.findViewById(R.id.tv_sale_total_amount);
            TextView tvSaleDate = saleCardView.findViewById(R.id.tv_sale_date);
            TextView tvSaleItemsCount = saleCardView.findViewById(R.id.tv_sale_items_count);
            MaterialButton btnCardReturn = saleCardView.findViewById(R.id.btn_card_return);

            String saleNum = sale.getSaleNumber() != null ? sale.getSaleNumber() : String.valueOf(sale.getId());
            tvSaleNumber.setText(String.format(Locale.getDefault(), "Sale #%s", saleNum));
            tvSaleCustomer.setText(String.format(Locale.getDefault(), "Customer: %s",
                    sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in Customer"));
            tvSaleTotalAmount.setText(String.format(Locale.getDefault(), "$%.2f", sale.getTotalAmount()));
            tvSaleDate.setText(sale.getSaleDate() != null ? sale.getSaleDate() : "Recent");
            int itemsCount = sale.getItems() != null ? sale.getItems().size() : 0;
            tvSaleItemsCount.setText(String.format(Locale.getDefault(), "%d item(s)", itemsCount));

            btnCardReturn.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ReturnProcessActivity.class);
                intent.putExtra("EXTRA_SALE_ID", sale.getId());
                startActivity(intent);
            });

            layoutRecentSalesContainer.addView(saleCardView);
            count++;
        }
    }
}