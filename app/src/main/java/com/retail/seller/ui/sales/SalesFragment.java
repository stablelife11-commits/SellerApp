package com.retail.seller.ui.sales;

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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.retail.seller.R;
import com.retail.seller.data.model.PaymentRequestDto;
import com.retail.seller.data.model.PaymentResponseDto;
import com.retail.seller.data.model.SaleItemDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.model.SellerOrderUpdateRequestDto;
import com.retail.seller.data.network.ApiClient;
import com.retail.seller.data.network.TokenManager;
import com.retail.seller.ui.returns.ReturnProcessActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalesFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvSales;
    private LinearLayout layoutEmpty;
    private ProgressBar salesProgress;
    private TextView tvSalesCountSummary;
    private TextInputEditText etSearchOrders;
    private ChipGroup chipGroupStatus;
    private SaleAdapter adapter;

    private final List<SaleResponseDto> allOrders = new ArrayList<>();
    private int currentStatusChipId = R.id.chip_order_all;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipe_refresh_sales);
        rvSales = view.findViewById(R.id.rv_sales);
        layoutEmpty = view.findViewById(R.id.layout_empty_sales);
        salesProgress = view.findViewById(R.id.sales_progress);
        tvSalesCountSummary = view.findViewById(R.id.tv_sales_count_summary);
        etSearchOrders = view.findViewById(R.id.et_search_orders);
        chipGroupStatus = view.findViewById(R.id.chip_group_order_status);

        rvSales.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SaleAdapter(requireContext(), new ArrayList<>(), new SaleAdapter.OnOrderActionListener() {
            @Override
            public void onReturnClick(SaleResponseDto order) {
                Intent intent = new Intent(requireContext(), ReturnProcessActivity.class);
                intent.putExtra("EXTRA_ORDER_ID", order.getId());
                intent.putExtra(ReturnProcessActivity.EXTRA_ORDER_JSON, new Gson().toJson(order));
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    intent.putExtra("EXTRA_VARIANT_ID", order.getItems().get(0).getVariantId());
                }
                startActivity(intent);
            }

            @Override
            public void onStatusTransitionSelected(SaleResponseDto order, String newStatus) {
                updateCustomerOrderStatus(order.getId(), newStatus);
            }

            @Override
            public void onOrderModified(Long orderId, SellerOrderUpdateRequestDto updateRequest) {
                modifyCustomerOrder(orderId, updateRequest);
            }

            @Override
            public void onRecordPayment(PaymentRequestDto paymentRequest) {
                recordCustomerPayment(paymentRequest);
            }
        });

        rvSales.setAdapter(adapter);
        swipeRefresh.setOnRefreshListener(this::fetchSales);

        if (etSearchOrders != null) {
            etSearchOrders.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyOrderFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (chipGroupStatus != null) {
            chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
                currentStatusChipId = checkedIds.isEmpty() ? R.id.chip_order_all : checkedIds.get(0);
                applyOrderFilters();
            });
        }

        fetchSales();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchSales();
    }

    private void recordCustomerPayment(PaymentRequestDto paymentRequest) {
        if (!isAdded()) return;
        salesProgress.setVisibility(View.VISIBLE);

        ApiClient.getApiService(requireContext()).recordPayment(paymentRequest).enqueue(new Callback<PaymentResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<PaymentResponseDto> call, @NonNull Response<PaymentResponseDto> response) {
                if (!isAdded()) return;
                salesProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(),
                            String.format(Locale.getDefault(), "Payment of ₹%.2f recorded successfully!", response.body().getAmount()),
                            Toast.LENGTH_LONG).show();
                    fetchSales();
                } else {
                    String err = ApiClient.extractErrorMessage(response, "Failed to record payment");
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentResponseDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                salesProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void modifyCustomerOrder(Long orderId, SellerOrderUpdateRequestDto updateRequest) {
        if (!isAdded()) return;
        salesProgress.setVisibility(View.VISIBLE);

        ApiClient.getApiService(requireContext()).modifyOrder(orderId, updateRequest).enqueue(new Callback<SaleResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<SaleResponseDto> call, @NonNull Response<SaleResponseDto> response) {
                if (!isAdded()) return;
                salesProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(),
                            "Order #" + orderId + " updated (" + response.body().getStatus() + ")! Final Total: ₹" + response.body().getTotalAmount(),
                            Toast.LENGTH_LONG).show();
                    fetchSales();
                } else {
                    String err = ApiClient.extractErrorMessage(response, "Failed to modify order");
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SaleResponseDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                salesProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCustomerOrderStatus(Long orderId, String targetStatus) {
        if (!isAdded()) return;
        salesProgress.setVisibility(View.VISIBLE);

        ApiClient.getApiService(requireContext()).updateOrderStatus(orderId, targetStatus).enqueue(new Callback<SaleResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<SaleResponseDto> call, @NonNull Response<SaleResponseDto> response) {
                if (!isAdded()) return;
                salesProgress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Order #" + orderId + " updated to " + targetStatus + "!", Toast.LENGTH_SHORT).show();
                    fetchSales();
                } else {
                    String err = ApiClient.extractErrorMessage(response, "Failed to update order status");
                    Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SaleResponseDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                salesProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSales() {
        if (!isAdded()) return;
        if (!swipeRefresh.isRefreshing() && salesProgress != null) {
            salesProgress.setVisibility(View.VISIBLE);
        }

        TokenManager tm = TokenManager.getInstance(requireContext());
        Long sellerId = tm.getUserId();
        if (sellerId == null) {
            hideLoading();
            allOrders.clear();
            displaySales(Collections.emptyList());
            return;
        }

        Call<List<SaleResponseDto>> call = tm.isAdmin()
                ? ApiClient.getApiService(requireContext()).getAllOrdersForAdmin(0, 50)
                : ApiClient.getApiService(requireContext()).getSellerOrders(sellerId, 0, 50);

        call.enqueue(new Callback<List<SaleResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<SaleResponseDto>> call, @NonNull Response<List<SaleResponseDto>> response) {
                if (!isAdded()) return;
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    allOrders.clear();
                    allOrders.addAll(response.body());
                    applyOrderFilters();
                } else {
                    allOrders.clear();
                    displaySales(Collections.emptyList());
                    String err = ApiClient.extractErrorMessage(response, "Could not load orders");
                    Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SaleResponseDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                hideLoading();
                allOrders.clear();
                displaySales(Collections.emptyList());
                Toast.makeText(requireContext(), "Connection failed: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyOrderFilters() {
        String query = etSearchOrders != null && etSearchOrders.getText() != null
                ? etSearchOrders.getText().toString().trim().toLowerCase(Locale.ROOT) : "";

        List<SaleResponseDto> filtered = new ArrayList<>();
        for (SaleResponseDto order : allOrders) {
            String status = order.getStatus().toUpperCase(Locale.ROOT);
            if (!matchesStatusChip(status)) continue;

            if (query.isEmpty()) {
                filtered.add(order);
                continue;
            }

            String idStr = String.valueOf(order.getId());
            String custName = order.getCustomerName() != null ? order.getCustomerName().toLowerCase(Locale.ROOT) : "";
            String custMob = order.getCustomerMobile() != null ? order.getCustomerMobile().toLowerCase(Locale.ROOT) : "";

            boolean matchesHeader = idStr.contains(query) || custName.contains(query) || custMob.contains(query);
            boolean matchesItem = false;
            if (order.getItems() != null) {
                for (SaleItemDto item : order.getItems()) {
                    if ((item.getProductName() != null && item.getProductName().toLowerCase(Locale.ROOT).contains(query))
                            || (item.getSku() != null && item.getSku().toLowerCase(Locale.ROOT).contains(query))) {
                        matchesItem = true;
                        break;
                    }
                }
            }

            if (matchesHeader || matchesItem) {
                filtered.add(order);
            }
        }
        displaySales(filtered);
    }

    private boolean matchesStatusChip(String status) {
        if (currentStatusChipId == R.id.chip_order_all) return true;
        if (currentStatusChipId == R.id.chip_order_pending) return "PLACED".equals(status);
        if (currentStatusChipId == R.id.chip_order_confirmed) return "CONFIRMED".equals(status);
        if (currentStatusChipId == R.id.chip_order_processing) return "PROCESSING".equals(status);
        if (currentStatusChipId == R.id.chip_order_shipped) return "SHIPPED".equals(status);
        if (currentStatusChipId == R.id.chip_order_delivered) return "DELIVERED".equals(status);
        return true;
    }

    private void hideLoading() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        if (salesProgress != null) salesProgress.setVisibility(View.GONE);
    }

    private void displaySales(List<SaleResponseDto> sales) {
        if (sales == null || sales.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvSales.setVisibility(View.GONE);
            tvSalesCountSummary.setText(String.format(Locale.getDefault(), "Showing 0 of %d order(s)", allOrders.size()));
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvSales.setVisibility(View.VISIBLE);
            adapter.updateSales(sales);
            tvSalesCountSummary.setText(String.format(Locale.getDefault(),
                    "Showing %d of %d order(s) • Tap any card for full workflow", sales.size(), allOrders.size()));
        }
    }
}