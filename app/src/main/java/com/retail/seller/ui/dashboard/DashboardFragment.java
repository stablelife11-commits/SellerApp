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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import com.retail.seller.R;
import com.retail.seller.data.model.DashboardSummaryDto;
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

    private SwipeRefreshLayout swipeRefreshDashboard;
    private TextView tvActiveProductsCount;
    private TextView tvLowStockCount;
    private TextView tvTotalRevenue;
    private TextView tvRevenueBreakdown;
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

        swipeRefreshDashboard = view.findViewById(R.id.swipe_refresh_dashboard);
        tvWelcomeTitle = view.findViewById(R.id.tv_welcome_title);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvActiveProductsCount = view.findViewById(R.id.tv_active_products_count);
        tvLowStockCount = view.findViewById(R.id.tv_low_stock_count);
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        tvRevenueBreakdown = view.findViewById(R.id.tv_revenue_breakdown);
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
            tvUserEmail.setText(tokenManager.getEmail() + " • " + tokenManager.getRole());
        }

        swipeRefreshDashboard.setOnRefreshListener(this::loadDashboardData);

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
        if (!isAdded()) return;
        if (!swipeRefreshDashboard.isRefreshing() && dashboardProgress != null) {
            dashboardProgress.setVisibility(View.VISIBLE);
        }

        // 1. Fetch authoritative financial summary from /api/v1/reports/dashboard-summary
        ApiClient.getApiService(requireContext()).getDashboardSummary().enqueue(new Callback<DashboardSummaryDto>() {
            @Override
            public void onResponse(@NonNull Call<DashboardSummaryDto> call, @NonNull Response<DashboardSummaryDto> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    DashboardSummaryDto summary = response.body();
                    tvTotalRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", summary.getTotalSalesAmount()));
                    tvRevenueBreakdown.setText(String.format(Locale.getDefault(),
                            "Online Delivered: ₹%.2f | POS: ₹%.2f | Returns: ₹%.2f | Customers: %d",
                            summary.getOnlineOrderSalesAmount(),
                            summary.getPosSalesAmount(),
                            summary.getTotalReturnsAmount(),
                            summary.getTotalCustomers()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardSummaryDto> call, @NonNull Throwable t) {
            }
        });

        // 2. Fetch Seller's own catalog for Active Products & Low Stock counts
        ApiClient.getApiService(requireContext()).getMyProducts(0, 100).enqueue(new Callback<List<ProductResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductResponseDto>> call, @NonNull Response<List<ProductResponseDto>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    updateProductStats(response.body());
                } else {
                    updateProductStats(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductResponseDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                updateProductStats(Collections.emptyList());
            }
        });

        // 3. Fetch Seller's Recent Buyer Orders using actual authenticated userId
        TokenManager tm = TokenManager.getInstance(requireContext());
        Long sellerId = tm.getUserId();
        if (sellerId == null) {
            hideLoading();
            return;
        }

        Call<List<SaleResponseDto>> ordersCall = tm.isAdmin()
                ? ApiClient.getApiService(requireContext()).getAllOrdersForAdmin(0, 10)
                : ApiClient.getApiService(requireContext()).getSellerOrders(sellerId, 0, 10);

        ordersCall.enqueue(new Callback<List<SaleResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<SaleResponseDto>> call, @NonNull Response<List<SaleResponseDto>> response) {
                if (!isAdded()) return;
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    updateRecentOrdersList(response.body());
                } else {
                    updateRecentOrdersList(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SaleResponseDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                hideLoading();
                updateRecentOrdersList(Collections.emptyList());
            }
        });
    }

    private void hideLoading() {
        if (swipeRefreshDashboard != null) swipeRefreshDashboard.setRefreshing(false);
        if (dashboardProgress != null) dashboardProgress.setVisibility(View.GONE);
    }

    private void updateProductStats(List<ProductResponseDto> products) {
        if (products == null) return;
        int activeProducts = 0;
        int lowStockCount = 0;

        for (ProductResponseDto product : products) {
            if (Boolean.TRUE.equals(product.getActive())) {
                activeProducts++;
            }
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

    private void updateRecentOrdersList(List<SaleResponseDto> orders) {
        if (orders == null || getContext() == null) return;

        layoutRecentSalesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (orders.isEmpty()) {
            TextView emptyTv = new TextView(getContext());
            emptyTv.setText("No buyer orders received yet.");
            emptyTv.setPadding(8, 16, 8, 16);
            layoutRecentSalesContainer.addView(emptyTv);
            return;
        }

        int count = 0;
        for (SaleResponseDto order : orders) {
            if (count >= 5) break;

            View cardView = inflater.inflate(R.layout.item_sale_card, layoutRecentSalesContainer, false);
            TextView tvSaleNumber = cardView.findViewById(R.id.tv_sale_number);
            TextView tvSaleCustomer = cardView.findViewById(R.id.tv_sale_customer);
            TextView tvSaleTotalAmount = cardView.findViewById(R.id.tv_sale_total_amount);
            TextView tvSaleDate = cardView.findViewById(R.id.tv_sale_date);
            TextView tvSaleItemsCount = cardView.findViewById(R.id.tv_sale_items_count);
            MaterialButton btnCardReturn = cardView.findViewById(R.id.btn_card_return);

            String status = order.getStatus();
            tvSaleNumber.setText(String.format(Locale.getDefault(), "Order #%d • %s", order.getId(), status));
            tvSaleCustomer.setText(String.format(Locale.getDefault(), "Buyer: %s (%s)",
                    order.getCustomerName() != null ? order.getCustomerName() : "Buyer",
                    order.getCustomerMobile() != null ? order.getCustomerMobile() : "N/A"));
            tvSaleTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount()));
            tvSaleDate.setText(order.getSaleDate() != null ? order.getSaleDate().replace("T", " ") : "Recent");
            int itemsCount = order.getItems() != null ? order.getItems().size() : 0;
            tvSaleItemsCount.setText(String.format(Locale.getDefault(), "%d item(s)", itemsCount));

            if ("DELIVERED".equalsIgnoreCase(status)) {
                btnCardReturn.setVisibility(View.VISIBLE);
                btnCardReturn.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), ReturnProcessActivity.class);
                    intent.putExtra("EXTRA_ORDER_ID", order.getId());
                    if (order.getItems() != null && !order.getItems().isEmpty()) {
                        intent.putExtra("EXTRA_VARIANT_ID", order.getItems().get(0).getVariantId());
                    }
                    startActivity(intent);
                });
            } else {
                btnCardReturn.setVisibility(View.GONE);
            }

            layoutRecentSalesContainer.addView(cardView);
            count++;
        }
    }
}