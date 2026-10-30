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

    public interface OnOrderActionListener {
        void onReturnClick(SaleResponseDto order);
        void onStatusTransitionSelected(SaleResponseDto order, String newStatus);
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
        SaleResponseDto order = sales.get(position);

        String orderNum = order.getSaleNumber() != null ? order.getSaleNumber() : String.valueOf(order.getId());
        String currentStatus = order.getStatus();
        String paymentStatus = order.getPaymentStatus();

        holder.tvSaleNumber.setText(String.format(Locale.getDefault(), "Order #%s • %s", orderNum, currentStatus));
        holder.tvSaleCustomer.setText(String.format(Locale.getDefault(), "Buyer: %s (%s)",
                order.getCustomerName() != null ? order.getCustomerName() : "Buyer",
                order.getCustomerMobile() != null ? order.getCustomerMobile() : "N/A"));
        holder.tvSaleTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount()));

        String dateStr = order.getSaleDate() != null ? order.getSaleDate().replace("T", " ") : "Recent";
        holder.tvSaleDate.setText(String.format(Locale.getDefault(), "%s | Pay: %s", dateStr, paymentStatus));

        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.tvSaleItemsCount.setText(String.format(Locale.getDefault(), "%d item(s) • Tap to Manage", itemCount));

        holder.itemView.setOnClickListener(v -> showOrderDetailsAndWorkflowDialog(order, orderNum, currentStatus));

        // Only allow Return processing on DELIVERED orders (as required by backend ReturnService)
        if ("DELIVERED".equalsIgnoreCase(currentStatus)) {
            holder.btnCardReturn.setVisibility(View.VISIBLE);
            holder.btnCardReturn.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onReturnClick(order);
                }
            });
        } else {
            holder.btnCardReturn.setVisibility(View.GONE);
        }
    }

    private void showOrderDetailsAndWorkflowDialog(SaleResponseDto order, String orderNum, String currentStatus) {
        StringBuilder details = new StringBuilder();
        details.append("👤 Buyer: ").append(order.getCustomerName() != null ? order.getCustomerName() : "N/A").append("\n");
        details.append("📞 Mobile: ").append(order.getCustomerMobile() != null ? order.getCustomerMobile() : "N/A").append("\n");
        details.append("📍 Delivery Address: ").append(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "N/A").append("\n");
        details.append("📌 Order Status: ").append(currentStatus).append("\n");
        details.append("💳 Payment: ").append(order.getPaymentStatus())
                .append(" (Paid: ₹").append(String.format(Locale.getDefault(), "%.2f", order.getPaidAmount())).append(")\n");
        details.append("--------------------------------------------------\n");

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (int i = 0; i < order.getItems().size(); i++) {
                SaleItemDto item = order.getItems().get(i);
                details.append(i + 1).append(". ").append(item.getProductName())
                        .append(" (Variant ID: ").append(item.getVariantId()).append(")\n");
                String size = item.getSize() != null ? item.getSize() : "Std";
                String color = item.getColor() != null ? item.getColor() : "Std";
                details.append("   Size: ").append(size).append(" | Color: ").append(color).append("\n");
                details.append("   Qty: ").append(item.getQuantity())
                        .append(" × ₹").append(String.format(Locale.getDefault(), "%.2f", item.getPrice())).append("\n\n");
            }
        } else {
            details.append("No line items found.\n");
        }
        details.append("--------------------------------------------------\n");
        details.append("💰 TOTAL AMOUNT: ₹").append(String.format(Locale.getDefault(), "%.2f", order.getTotalAmount()));

        String[] validNextStates = getValidNextStatuses(currentStatus);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Order #" + orderNum)
                .setMessage(details.toString())
                .setNegativeButton("Close", null);

        if (validNextStates.length > 0) {
            builder.setPositiveButton("Update Status (" + validNextStates[0] + "…)", (dialog, which) ->
                    showStatusTransitionPicker(order, currentStatus, validNextStates));
        }

        if ("DELIVERED".equalsIgnoreCase(currentStatus)) {
            builder.setNeutralButton("Process Return", (dialog, which) -> {
                if (actionListener != null) actionListener.onReturnClick(order);
            });
        }

        builder.show();
    }

    private void showStatusTransitionPicker(SaleResponseDto order, String currentStatus, String[] validNextStates) {
        new AlertDialog.Builder(context)
                .setTitle("Move Order #" + order.getId() + " from " + currentStatus + " to:")
                .setItems(validNextStates, (dialog, which) -> {
                    String selectedStatus = validNextStates[which];
                    if (actionListener != null) {
                        actionListener.onStatusTransitionSelected(order, selectedStatus);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Matches backend Order.OrderStatus.canTransitionTo(target) rules exactly
    private String[] getValidNextStatuses(String currentStatus) {
        if (currentStatus == null) return new String[0];
        switch (currentStatus.toUpperCase(Locale.ROOT)) {
            case "PLACED":
                return new String[]{"CONFIRMED", "PROCESSING", "REJECTED", "CANCELLED"};
            case "CONFIRMED":
                return new String[]{"PROCESSING", "SHIPPED", "CANCELLED"};
            case "PROCESSING":
                return new String[]{"SHIPPED", "CANCELLED"};
            case "SHIPPED":
                return new String[]{"DELIVERED"};
            default:
                return new String[0];
        }
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