package com.retail.seller.ui.sales;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.retail.seller.R;
import com.retail.seller.data.model.SaleResponseDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.SaleViewHolder> {

    public interface OnReturnClickListener {
        void onReturnClick(SaleResponseDto sale);
    }

    private final Context context;
    private final List<SaleResponseDto> sales;
    private final OnReturnClickListener returnClickListener;

    public SaleAdapter(Context context, List<SaleResponseDto> sales, OnReturnClickListener returnClickListener) {
        this.context = context;
        this.sales = sales != null ? sales : new ArrayList<>();
        this.returnClickListener = returnClickListener;
    }

    public void updateSales(List<SaleResponseDto> newSales) {
        this.sales.clear();
        if (newSales != null) {
            this.sales.addAll(newSales);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_card, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        SaleResponseDto sale = sales.get(position);

        String saleNum = sale.getSaleNumber() != null ? sale.getSaleNumber() : String.valueOf(sale.getId());
        holder.tvSaleNumber.setText(String.format(Locale.getDefault(), "Sale #%s", saleNum));

        holder.tvSaleCustomer.setText(String.format(Locale.getDefault(), "Customer: %s",
                sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in Customer"));

        holder.tvSaleTotalAmount.setText(String.format(Locale.getDefault(), "$%.2f", sale.getTotalAmount()));

        holder.tvSaleDate.setText(sale.getSaleDate() != null ? sale.getSaleDate() : "Recent");

        int itemCount = sale.getItems() != null ? sale.getItems().size() : 0;
        holder.tvSaleItemsCount.setText(String.format(Locale.getDefault(), "%d item(s)", itemCount));

        holder.btnCardReturn.setOnClickListener(v -> {
            if (returnClickListener != null) {
                returnClickListener.onReturnClick(sale);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sales.size();
    }

    static class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView tvSaleNumber;
        TextView tvSaleCustomer;
        TextView tvSaleTotalAmount;
        TextView tvSaleDate;
        TextView tvSaleItemsCount;
        Button btnCardReturn;

        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSaleNumber = itemView.findViewById(R.id.tv_sale_number);
            tvSaleCustomer = itemView.findViewById(R.id.tv_sale_customer);
            tvSaleTotalAmount = itemView.findViewById(R.id.tv_sale_total_amount);
            tvSaleDate = itemView.findViewById(R.id.tv_sale_date);
            tvSaleItemsCount = itemView.findViewById(R.id.tv_sale_items_count);
            btnCardReturn = itemView.findViewById(R.id.btn_card_return);
        }
    }
}