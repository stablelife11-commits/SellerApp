package com.retail.seller.ui.dashboard;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.retail.seller.MainActivity;
import com.retail.seller.R;
import com.retail.seller.data.model.DashboardSummaryDto;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import com.retail.seller.data.model.SaleItemDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.network.ApiClient;
import com.retail.seller.data.network.TokenManager;
import com.retail.seller.ui.auth.LoginActivity;
import com.retail.seller.ui.products.AddProductActivity;
import com.retail.seller.ui.returns.ReturnProcessActivity;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshDashboard;
    private TextView tvWelcomeTitle, tvUserEmail;
    private TextView tvMonthlyRevenue, tvTotalRevenue, tvRevenueBreakdown;
    private TextView tvTodaySales, tvTodayOrdersSub;
    private TextView tvTotalOrdersCount, tvDeliveredOrdersSub;
    private TextView tvPendingOrdersCount;
    private TextView tvActiveProductsCount, tvLowStockCount, tvOutOfStockSub;
    private TextView tvSevenDayTotal;
    private LinearLayout layoutSalesChartBars;
    private LinearLayout layoutBestSellingContainer;
    private LinearLayout layoutLowStockAlertsContainer;
    private LinearLayout layoutRecentSalesContainer;
    private ProgressBar dashboardProgress;

    private int cachedActiveProducts = 0;
    private DashboardSummaryDto cachedSummary = null;

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
        tvMonthlyRevenue = view.findViewById(R.id.tv_monthly_revenue);
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        tvRevenueBreakdown = view.findViewById(R.id.tv_revenue_breakdown);

        tvTodaySales = view.findViewById(R.id.tv_today_sales);
        tvTodayOrdersSub = view.findViewById(R.id.tv_today_orders_sub);
        tvTotalOrdersCount = view.findViewById(R.id.tv_total_orders_count);
        tvDeliveredOrdersSub = view.findViewById(R.id.tv_delivered_orders_sub);
        tvPendingOrdersCount = view.findViewById(R.id.tv_pending_orders_count);

        tvActiveProductsCount = view.findViewById(R.id.tv_active_products_count);
        tvLowStockCount = view.findViewById(R.id.tv_low_stock_count);
        tvOutOfStockSub = view.findViewById(R.id.tv_out_of_stock_sub);

        tvSevenDayTotal = view.findViewById(R.id.tv_seven_day_total);
        layoutSalesChartBars = view.findViewById(R.id.layout_sales_chart_bars);
        layoutBestSellingContainer = view.findViewById(R.id.layout_best_selling_container);
        layoutLowStockAlertsContainer = view.findViewById(R.id.layout_low_stock_alerts_container);
        layoutRecentSalesContainer = view.findViewById(R.id.layout_recent_sales_container);
        dashboardProgress = view.findViewById(R.id.dashboard_progress);

        MaterialButton btnAddProduct = view.findViewById(R.id.btn_action_add_product);
        MaterialButton btnProcessReturn = view.findViewById(R.id.btn_action_process_return);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);

        MaterialCardView cardPendingOrders = view.findViewById(R.id.card_kpi_pending_orders);
        MaterialCardView cardLowStock = view.findViewById(R.id.card_kpi_low_stock);
        TextView tvViewAllProducts = view.findViewById(R.id.tv_view_all_products);
        TextView tvManageInventory = view.findViewById(R.id.tv_manage_inventory_link);
        TextView tvViewAllOrders = view.findViewById(R.id.tv_view_all_orders);

        bindGreetingHeader();

        swipeRefreshDashboard.setOnRefreshListener(this::loadDashboardData);

        btnLogout.setOnClickListener(v -> {
            TokenManager.getInstance(requireContext()).clearSession();
            Toast.makeText(requireContext(), "Signed out successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnAddProduct.setOnClickListener(v -> startActivity(new Intent(requireContext(), AddProductActivity.class)));
        btnProcessReturn.setOnClickListener(v -> startActivity(new Intent(requireContext(), ReturnProcessActivity.class)));

        View.OnClickListener goToOrders = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_sales);
            }
        };
        View.OnClickListener goToProducts = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_products);
            }
        };
        View.OnClickListener goToInventory = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_inventory);
            }
        };

        if (cardPendingOrders != null) cardPendingOrders.setOnClickListener(goToOrders);
        if (tvViewAllOrders != null) tvViewAllOrders.setOnClickListener(goToOrders);
        if (tvViewAllProducts != null) tvViewAllProducts.setOnClickListener(goToProducts);
        if (cardLowStock != null) cardLowStock.setOnClickListener(goToInventory);
        if (tvManageInventory != null) tvManageInventory.setOnClickListener(goToInventory);

        loadDashboardData();
    }

    private void bindGreetingHeader() {
        TokenManager tm = TokenManager.getInstance(requireContext());
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good Morning" : (hour < 17 ? "Good Afternoon" : "Good Evening");
        String sellerName = tm.getName() != null && !tm.getName().isEmpty() ? tm.getName() : "Seller";
        tvWelcomeTitle.setText(greeting + ", " + sellerName);

        String businessName = tm.getBusinessName();
        String emailRole = (tm.getEmail() != null ? tm.getEmail() : "") + " • " + (tm.getRole() != null ? tm.getRole() : "SELLER");
        tvUserEmail.setText(businessName + " | " + emailRole);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindGreetingHeader();
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
                    cachedSummary = response.body();
                    updateHeroSummaryText();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardSummaryDto> call, @NonNull Throwable t) {
            }
        });

        // 2. Fetch Seller's catalog for Active Products & Low Stock Alerts
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

        // 3. Fetch Seller's Orders (up to 100) for Today's Sales, Monthly Revenue, Analytics Chart, Best Sellers & Recent Orders
        TokenManager tm = TokenManager.getInstance(requireContext());
        Long sellerId = tm.getUserId();
        if (sellerId == null) {
            hideLoading();
            return;
        }

        Call<List<SaleResponseDto>> ordersCall = tm.isAdmin()
                ? ApiClient.getApiService(requireContext()).getAllOrdersForAdmin(0, 100)
                : ApiClient.getApiService(requireContext()).getSellerOrders(sellerId, 0, 100);

        ordersCall.enqueue(new Callback<List<SaleResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<SaleResponseDto>> call, @NonNull Response<List<SaleResponseDto>> response) {
                if (!isAdded()) return;
                hideLoading();
                List<SaleResponseDto> orders = (response.isSuccessful() && response.body() != null)
                        ? response.body()
                        : Collections.emptyList();
                computeAndBindOrderAnalytics(orders);
                updateRecentOrdersList(orders);
            }

            @Override
            public void onFailure(@NonNull Call<List<SaleResponseDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                hideLoading();
                computeAndBindOrderAnalytics(Collections.emptyList());
                updateRecentOrdersList(Collections.emptyList());
            }
        });
    }

    private void updateHeroSummaryText() {
        if (cachedSummary == null) return;
        tvTotalRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", cachedSummary.getTotalSalesAmount()));
        tvRevenueBreakdown.setText(String.format(Locale.getDefault(),
                "Online: ₹%.0f • POS: ₹%.0f • Buyers: %d • Active Products: %d",
                cachedSummary.getOnlineOrderSalesAmount(),
                cachedSummary.getPosSalesAmount(),
                cachedSummary.getTotalCustomers(),
                cachedActiveProducts > 0 ? cachedActiveProducts : cachedSummary.getTotalProducts()));
    }

    private void updateProductStats(List<ProductResponseDto> products) {
        if (products == null || getContext() == null) return;
        int activeProducts = 0;
        int lowStockCount = 0;
        int outOfStockCount = 0;

        layoutLowStockAlertsContainer.removeAllViews();
        List<String> lowStockLines = new ArrayList<>();

        for (ProductResponseDto product : products) {
            if (Boolean.TRUE.equals(product.getActive())) {
                activeProducts++;
            }
            if (product.getVariants() != null) {
                for (ProductVariantDto variant : product.getVariants()) {
                    int stock = variant.getStock();
                    if (stock <= 0) {
                        outOfStockCount++;
                        lowStockCount++;
                    } else if (stock < 10) {
                        lowStockCount++;
                    }
                    if (stock < 10 && lowStockLines.size() < 4) {
                        String pName = product.getName() != null ? product.getName() : "Product";
                        String sz = variant.getSize() != null ? variant.getSize() : "Std";
                        String col = variant.getColor() != null ? variant.getColor() : "Std";
                        lowStockLines.add(pName + " (" + sz + " • " + col + ") — SKU: " + variant.getSku() + " | Stock: " + stock);
                    }
                }
            }
        }

        cachedActiveProducts = activeProducts;
        if (tvActiveProductsCount != null) {
            tvActiveProductsCount.setText(String.valueOf(activeProducts));
        }
        if (tvLowStockCount != null) {
            tvLowStockCount.setText(String.valueOf(lowStockCount));
        }
        if (tvOutOfStockSub != null) {
            tvOutOfStockSub.setText(String.format(Locale.getDefault(), "%d out of stock • Tap to restock", outOfStockCount));
        }
        updateHeroSummaryText();

        if (lowStockLines.isEmpty()) {
            TextView tvOk = new TextView(requireContext());
            tvOk.setText("✓ All product variants are well-stocked (10+ units).");
            tvOk.setTextColor(ContextCompat.getColor(requireContext(), R.color.emerald_dark));
            tvOk.setTextSize(13f);
            tvOk.setPadding(8, 8, 8, 8);
            layoutLowStockAlertsContainer.addView(tvOk);
        } else {
            for (String alertLine : lowStockLines) {
                TextView row = new TextView(requireContext());
                row.setText("⚠ " + alertLine);
                row.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_red));
                row.setTextSize(12.5f);
                row.setPadding(8, 6, 8, 6);
                layoutLowStockAlertsContainer.addView(row);
            }
        }
    }

    private void computeAndBindOrderAnalytics(List<SaleResponseDto> orders) {
        if (getContext() == null) return;

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        double todaySales = 0.0;
        int todayOrders = 0;
        double monthlyRevenue = 0.0;
        int totalOrders = orders != null ? orders.size() : 0;
        int pendingOrders = 0;
        int deliveredOrders = 0;

        Map<LocalDate, Double> last7DaysRevenue = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            last7DaysRevenue.put(today.minusDays(i), 0.0);
        }

        Map<String, BestSellerAggregate> bestSellersMap = new HashMap<>();

        if (orders != null) {
            for (SaleResponseDto order : orders) {
                String status = order.getStatus() != null ? order.getStatus().toUpperCase(Locale.ROOT) : "PLACED";
                boolean isCancelledOrRejected = "CANCELLED".equals(status) || "REJECTED".equals(status);

                if ("PLACED".equals(status)) {
                    pendingOrders++;
                }
                if ("DELIVERED".equals(status)) {
                    deliveredOrders++;
                }

                LocalDate orderDate = parseOrderLocalDate(order.getSaleDate());
                double amount = order.getTotalAmount();

                if (!isCancelledOrRejected) {
                    if (orderDate != null) {
                        if (orderDate.equals(today)) {
                            todaySales += amount;
                            todayOrders++;
                        }
                        if (YearMonth.from(orderDate).equals(currentMonth)) {
                            monthlyRevenue += amount;
                        }
                        if (last7DaysRevenue.containsKey(orderDate)) {
                            last7DaysRevenue.put(orderDate, last7DaysRevenue.get(orderDate) + amount);
                        }
                    }

                    if (order.getItems() != null) {
                        for (SaleItemDto item : order.getItems()) {
                            String pName = item.getProductName();
                            BestSellerAggregate agg = bestSellersMap.computeIfAbsent(pName, BestSellerAggregate::new);
                            agg.unitsSold += item.getQuantity();
                            agg.revenue += (item.getQuantity() * item.getPrice());
                        }
                    }
                }
            }
        }

        tvTodaySales.setText(String.format(Locale.getDefault(), "₹%.2f", todaySales));
        tvTodayOrdersSub.setText(String.format(Locale.getDefault(), "%d order(s) today", todayOrders));
        tvMonthlyRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", monthlyRevenue));
        tvTotalOrdersCount.setText(String.valueOf(totalOrders));
        tvDeliveredOrdersSub.setText(String.format(Locale.getDefault(), "%d delivered", deliveredOrders));
        tvPendingOrdersCount.setText(String.valueOf(pendingOrders));

        renderSevenDayChart(today, last7DaysRevenue);
        renderBestSellingProducts(new ArrayList<>(bestSellersMap.values()));
    }

    private void renderSevenDayChart(LocalDate today, Map<LocalDate, Double> last7DaysRevenue) {
        if (layoutSalesChartBars == null || getContext() == null) return;
        layoutSalesChartBars.removeAllViews();

        double maxDayRev = 1.0;
        double sevenDaySum = 0.0;
        for (Double val : last7DaysRevenue.values()) {
            if (val != null) {
                sevenDaySum += val;
                if (val > maxDayRev) maxDayRev = val;
            }
        }

        tvSevenDayTotal.setText(String.format(Locale.getDefault(), "7d: ₹%.0f", sevenDaySum));
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE", Locale.getDefault());
        float density = getResources().getDisplayMetrics().density;

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            double rev = last7DaysRevenue.getOrDefault(date, 0.0);

            LinearLayout col = new LinearLayout(requireContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            col.setLayoutParams(colParams);

            TextView tvVal = new TextView(requireContext());
            tvVal.setText(rev >= 1000 ? String.format(Locale.getDefault(), "₹%.1fk", rev / 1000.0) : String.format(Locale.getDefault(), "₹%.0f", rev));
            tvVal.setTextSize(9.5f);
            tvVal.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvVal.setGravity(Gravity.CENTER);
            col.addView(tvVal);

            int maxBarHeightPx = (int) (84 * density);
            int minBarHeightPx = (int) (8 * density);
            int barHeightPx = rev <= 0 ? minBarHeightPx : Math.max(minBarHeightPx, (int) ((rev / maxDayRev) * maxBarHeightPx));

            View barView = new View(requireContext());
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams((int) (18 * density), barHeightPx);
            barParams.topMargin = (int) (4 * density);
            barParams.bottomMargin = (int) (6 * density);
            barView.setLayoutParams(barParams);

            GradientDrawable barBg = new GradientDrawable();
            barBg.setShape(GradientDrawable.RECTANGLE);
            barBg.setCornerRadius(8 * density);
            barBg.setColor(ContextCompat.getColor(requireContext(),
                    i == 0 ? R.color.royal_secondary : (rev > 0 ? R.color.navy_primary : R.color.card_stroke)));
            barView.setBackground(barBg);
            col.addView(barView);

            TextView tvDay = new TextView(requireContext());
            tvDay.setText(date.format(dayFmt));
            tvDay.setTextSize(11f);
            tvDay.setTypeface(null, i == 0 ? Typeface.BOLD : Typeface.NORMAL);
            tvDay.setTextColor(ContextCompat.getColor(requireContext(), i == 0 ? R.color.royal_secondary : R.color.text_secondary));
            tvDay.setGravity(Gravity.CENTER);
            col.addView(tvDay);

            layoutSalesChartBars.addView(col);
        }
    }

    private void renderBestSellingProducts(List<BestSellerAggregate> aggregates) {
        if (layoutBestSellingContainer == null || getContext() == null) return;
        layoutBestSellingContainer.removeAllViews();

        if (aggregates.isEmpty()) {
            TextView tvEmpty = new TextView(requireContext());
            tvEmpty.setText("Best-selling wholesale products will appear here as orders are placed.");
            tvEmpty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvEmpty.setTextSize(13f);
            tvEmpty.setPadding(8, 8, 8, 8);
            layoutBestSellingContainer.addView(tvEmpty);
            return;
        }

        aggregates.sort((a, b) -> Integer.compare(b.unitsSold, a.unitsSold));
        int count = Math.min(aggregates.size(), 4);

        for (int i = 0; i < count; i++) {
            BestSellerAggregate item = aggregates.get(i);
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(8, 10, 8, 10);

            TextView tvRankName = new TextView(requireContext());
            tvRankName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            tvRankName.setText(String.format(Locale.getDefault(), "#%d  %s", i + 1, item.productName));
            tvRankName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvRankName.setTextSize(13.5f);
            tvRankName.setTypeface(null, Typeface.BOLD);
            row.addView(tvRankName);

            TextView tvStats = new TextView(requireContext());
            tvStats.setText(String.format(Locale.getDefault(), "%d units • ₹%.0f", item.unitsSold, item.revenue));
            tvStats.setTextColor(ContextCompat.getColor(requireContext(), R.color.emerald_dark));
            tvStats.setTextSize(12.5f);
            tvStats.setTypeface(null, Typeface.BOLD);
            row.addView(tvStats);

            layoutBestSellingContainer.addView(row);
        }
    }

    private LocalDate parseOrderLocalDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return null;
        try {
            String datePart = rawDate.trim();
            if (datePart.length() >= 10) {
                datePart = datePart.substring(0, 10);
            }
            return LocalDate.parse(datePart);
        } catch (Exception e) {
            return null;
        }
    }

    private static class BestSellerAggregate {
        final String productName;
        int unitsSold = 0;
        double revenue = 0.0;

        BestSellerAggregate(String productName) {
            this.productName = productName;
        }
    }

    private void hideLoading() {
        if (swipeRefreshDashboard != null) swipeRefreshDashboard.setRefreshing(false);
        if (dashboardProgress != null) dashboardProgress.setVisibility(View.GONE);
    }

    private void updateRecentOrdersList(List<SaleResponseDto> orders) {
        if (orders == null || getContext() == null) return;

        layoutRecentSalesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (orders.isEmpty()) {
            TextView emptyTv = new TextView(getContext());
            emptyTv.setText("No buyer orders received yet.");
            emptyTv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
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
            TextView tvOrderStatusBadge = cardView.findViewById(R.id.tv_order_status_badge);
            TextView tvPaymentStatusBadge = cardView.findViewById(R.id.tv_payment_status_badge);
            View layoutQuickActions = cardView.findViewById(R.id.layout_order_quick_actions);
            MaterialButton btnCardReturn = cardView.findViewById(R.id.btn_card_return);

            if (layoutQuickActions != null) {
                layoutQuickActions.setVisibility(View.GONE);
            }

            String status = order.getStatus();
            tvSaleNumber.setText(String.format(Locale.getDefault(), "Order #%d", order.getId()));
            if (tvOrderStatusBadge != null) {
                tvOrderStatusBadge.setText(status);
            }
            if (tvPaymentStatusBadge != null) {
                tvPaymentStatusBadge.setText(order.getPaymentStatus());
            }
            tvSaleCustomer.setText(String.format(Locale.getDefault(), "Buyer: %s (%s)",
                    order.getCustomerName() != null ? order.getCustomerName() : "Buyer",
                    order.getCustomerMobile() != null ? order.getCustomerMobile() : "N/A"));
            tvSaleTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount()));
            tvSaleDate.setText(order.getSaleDate() != null ? order.getSaleDate().replace("T", " ") : "Recent");
            int itemsCount = order.getItems() != null ? order.getItems().size() : 0;
            tvSaleItemsCount.setText(String.format(Locale.getDefault(), "%d item(s)", itemsCount));

            cardView.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToTab(R.id.nav_sales);
                }
            });

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