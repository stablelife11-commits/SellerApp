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

    public SalesFragment() {
    }

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
        adapter = new SaleAdapter(requireContext(), new ArrayList<>(), sale -> {
            Intent intent = new Intent(requireContext(), ReturnProcessActivity.class);
            intent.putExtra("EXTRA_SALE_ID", sale.getId());
            startActivity(intent);
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

    private void fetchSales() {
        if (!swipeRefresh.isRefreshing() && salesProgress != null) {
            salesProgress.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService(requireContext()).getSales().enqueue(new Callback<List<SaleResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<SaleResponseDto>> call, @NonNull Response<List<SaleResponseDto>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    displaySales(response.body());
                } else {
                    displaySales(Collections.emptyList());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error fetching sales (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SaleResponseDto>> call, @NonNull Throwable t) {
                hideLoading();
                displaySales(Collections.emptyList());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to connect: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
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
            tvSalesCountSummary.setText(String.format(Locale.getDefault(), "%d item(s)", 0));
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvSales.setVisibility(View.VISIBLE);
            adapter.updateSales(sales);
            tvSalesCountSummary.setText(String.format(Locale.getDefault(), "%d item(s)", sales.size()));
        }
    }
}