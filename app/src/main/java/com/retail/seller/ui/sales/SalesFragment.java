package com.retail.seller.ui.sales;

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
import com.retail.seller.R;
import com.retail.seller.data.model.SaleResponseDto;
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
    private SaleAdapter adapter;

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

        rvSales.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SaleAdapter(requireContext(), new ArrayList<>(), new SaleAdapter.OnOrderActionListener() {
            @Override
            public void onReturnClick(SaleResponseDto order) {
                Intent intent = new Intent(requireContext(), ReturnProcessActivity.class);
                intent.putExtra("EXTRA_ORDER_ID", order.getId());
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    intent.putExtra("EXTRA_VARIANT_ID", order.getItems().get(0).getVariantId());
                }
                startActivity(intent);
            }

            @Override
            public void onStatusTransitionSelected(SaleResponseDto order, String newStatus) {
                updateCustomerOrderStatus(order.getId(), newStatus);
            }
        });

        rvSales.setAdapter(adapter);
        swipeRefresh.setOnRefreshListener(this::fetchSales);
        fetchSales();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchSales();
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
                    displaySales(response.body());
                } else {
                    displaySales(Collections.emptyList());
                    String err = ApiClient.extractErrorMessage(response, "Could not load orders");
                    Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SaleResponseDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                hideLoading();
                displaySales(Collections.emptyList());
                Toast.makeText(requireContext(), "Connection failed: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideLoading() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        if (salesProgress != null) salesProgress.setVisibility(View.GONE);
    }

    private void displaySales(List<SaleResponseDto> sales) {
        if (sales == null || sales.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvSales.setVisibility(View.GONE);
            tvSalesCountSummary.setText(String.format(Locale.getDefault(), "%d order(s)", 0));
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvSales.setVisibility(View.VISIBLE);
            adapter.updateSales(sales);
            tvSalesCountSummary.setText(String.format(Locale.getDefault(), "%d order(s) • Tap any order to update status", sales.size()));
        }
    }
}