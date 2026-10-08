package com.retail.seller.ui.sales;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.retail.seller.R;
import com.retail.seller.data.model.SaleItemDto;
import com.retail.seller.data.model.SaleResponseDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.SaleViewHolder> {

    // 🟢 FIX: Interface me Accept Order ka action joda gaya
    public interface OnOrderActionListener {
        void onReturnClick(SaleResponseDto sale);
        void onAcceptOrderClick(SaleResponseDto sale);
    }

    private final Context context;
    private final List<SaleResponseDto> sales;
    private final OnOrderActionListener actionListener;

    public SaleAdapter(Context context, List<SaleResponseDto> sales, OnOrderActionListener actionListener) {
        this.context = context;
        this.sales = sales != null ? sales : new ArrayList<>();
        this.actionListener = actionListener;
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
        holder.tvSaleNumber.setText(String.format(Locale.getDefault(), "Order #%s", saleNum));
        holder.tvSaleCustomer.setText(String.format(Locale.getDefault(), "Customer: %s",
                sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in Customer"));
        holder.tvSaleTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", sale.getTotalAmount()));
        holder.tvSaleDate.setText(sale.getSaleDate() != null ? sale.getSaleDate() : "Recent");

        int itemCount = sale.getItems() != null ? sale.getItems().size() : 0;
        holder.tvSaleItemsCount.setText(String.format(Locale.getDefault(), "%d item(s)", itemCount));

        // 🟢 INVOICE FORMAT POP-UP
        holder.itemView.setOnClickListener(v -> {
            StringBuilder invoice = new StringBuilder();

            // Customer Details
            invoice.append("👤 Customer Name: ").append(sale.getCustomerName()).append("\n");
            invoice.append("📞 Mobile: ").append(sale.getCustomerMobile() != null ? sale.getCustomerMobile() : "N/A").append("\n");
            invoice.append("📍 Address: ").append(sale.getDeliveryAddress() != null ? sale.getDeliveryAddress() : "N/A").append("\n");

            // Status
            String currentStatus = sale.getStatus() != null ? sale.getStatus() : "PLACED";
            invoice.append("📌 Status: ").append(currentStatus).append("\n");
            invoice.append("--------------------------------------------------\n");

            // Order Items & Size
            if (sale.getItems() != null && !sale.getItems().isEmpty()) {
                for (int i = 0; i < sale.getItems().size(); i++) {
                    SaleItemDto item = sale.getItems().get(i);
                    invoice.append(i + 1).append(". ").append(item.getProductName()).append("\n");

                    String size = item.getSize() != null ? item.getSize() : "N/A";
                    String color = item.getColor() != null ? item.getColor() : "N/A";
                    invoice.append("   Size: ").append(size).append(" | Color: ").append(color).append("\n");
                    invoice.append("   Qty: ").append(item.getQuantity()).append(" | Price: ₹").append(item.getPrice()).append("\n\n");
                }
            } else {
                invoice.append("No items found.\n");
            }
            invoice.append("--------------------------------------------------\n");
            invoice.append("💰 TOTAL PAYABLE: ₹").append(sale.getTotalAmount());

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Invoice - Order #" + saleNum)
                    .setMessage(invoice.toString());

            // 🟢 Agar order naya hai (PLACED), toh 'Accept' ka button dikhao
            if ("PLACED".equalsIgnoreCase(currentStatus)) {
                builder.setPositiveButton("ACCEPT ORDER", (dialog, which) -> {
                    if (actionListener != null) actionListener.onAcceptOrderClick(sale);
                });
                builder.setNegativeButton("Close", null);
            } else {
                builder.setPositiveButton("Close", null);
            }

            builder.show();
        });

        holder.btnCardReturn.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onReturnClick(sale);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sales.size();
    }

    static class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView tvSaleNumber, tvSaleCustomer, tvSaleTotalAmount, tvSaleDate, tvSaleItemsCount;
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