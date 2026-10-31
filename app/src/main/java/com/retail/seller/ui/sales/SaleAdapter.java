package com.retail.seller.ui.sales;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.retail.seller.R;
import com.retail.seller.data.model.SaleItemDto;
import com.retail.seller.data.model.SaleResponseDto;
import com.retail.seller.data.model.SellerOrderUpdateRequestDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.SaleViewHolder> {

    public interface OnOrderActionListener {
        void onReturnClick(SaleResponseDto order);
        void onStatusTransitionSelected(SaleResponseDto order, String newStatus);
        void onOrderModified(Long orderId, SellerOrderUpdateRequestDto updateRequest);
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
        holder.tvSaleItemsCount.setText(String.format(Locale.getDefault(), "%d item(s) • Tap to Review / Modify", itemCount));

        holder.itemView.setOnClickListener(v -> showOrderDetailsAndWorkflowDialog(order, orderNum, currentStatus));

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
        if (order.getSellerRemarks() != null && !order.getSellerRemarks().trim().isEmpty()) {
            details.append("💬 Seller Remarks: ").append(order.getSellerRemarks().trim()).append("\n");
        }
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
                        .append(" × ₹").append(String.format(Locale.getDefault(), "%.2f", item.getPrice())).append("\n");
                if (item.getItemNote() != null && !item.getItemNote().trim().isEmpty()) {
                    details.append("   📝 Buyer Note: \"").append(item.getItemNote().trim()).append("\"\n");
                }
                details.append("\n");
            }
        } else {
            details.append("No line items found.\n");
        }
        details.append("--------------------------------------------------\n");
        if (order.getDiscountAmount() > 0) {
            details.append("Subtotal: ₹").append(String.format(Locale.getDefault(), "%.2f", order.getSubtotalAmount())).append("\n");
            details.append("Discount: -₹").append(String.format(Locale.getDefault(), "%.2f", order.getDiscountAmount())).append("\n");
        }
        details.append("💰 TOTAL AMOUNT: ₹").append(String.format(Locale.getDefault(), "%.2f", order.getTotalAmount()));

        String[] validNextStates = getValidNextStatuses(currentStatus);
        boolean canModify = "PLACED".equalsIgnoreCase(currentStatus) || "CONFIRMED".equalsIgnoreCase(currentStatus);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Order #" + orderNum)
                .setMessage(details.toString())
                .setNegativeButton("Close", null);

        if (validNextStates.length > 0) {
            builder.setPositiveButton("Update Status", (dialog, which) ->
                    showStatusTransitionPicker(order, currentStatus, validNextStates));
        }

        if (canModify) {
            builder.setNeutralButton("Modify & Confirm", (dialog, which) ->
                    showModifyOrderDialog(order));
        } else if ("DELIVERED".equalsIgnoreCase(currentStatus)) {
            builder.setNeutralButton("Process Return", (dialog, which) -> {
                if (actionListener != null) actionListener.onReturnClick(order);
            });
        }

        builder.show();
    }

    private void showModifyOrderDialog(SaleResponseDto order) {
        ScrollView scrollView = new ScrollView(context);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * context.getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        scrollView.addView(root);

        TextView lblAddress = new TextView(context);
        lblAddress.setText("Delivery Address:");
        lblAddress.setTypeface(null, Typeface.BOLD);
        root.addView(lblAddress);

        EditText etAddress = new EditText(context);
        etAddress.setText(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");
        root.addView(etAddress);

        TextView lblDiscount = new TextView(context);
        lblDiscount.setText("Overall Bill Discount (₹):");
        lblDiscount.setTypeface(null, Typeface.BOLD);
        root.addView(lblDiscount);

        EditText etDiscount = new EditText(context);
        etDiscount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etDiscount.setText(String.valueOf(order.getDiscountAmount()));
        root.addView(etDiscount);

        TextView lblRemarks = new TextView(context);
        lblRemarks.setText("Seller Remarks (Visible to Buyer):");
        lblRemarks.setTypeface(null, Typeface.BOLD);
        root.addView(lblRemarks);

        EditText etRemarks = new EditText(context);
        etRemarks.setHint("e.g., Adjusted qty & applied special wholesale discount");
        etRemarks.setText(order.getSellerRemarks() != null ? order.getSellerRemarks() : "");
        root.addView(etRemarks);

        TextView lblItems = new TextView(context);
        lblItems.setText("\nOrder Line Items (Set Qty = 0 to remove item):");
        lblItems.setTypeface(null, Typeface.BOLD);
        root.addView(lblItems);

        LinearLayout itemsContainer = new LinearLayout(context);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(itemsContainer);

        List<ItemRowHolder> rowHolders = new ArrayList<>();
        if (order.getItems() != null) {
            for (SaleItemDto item : order.getItems()) {
                addEditableItemRow(itemsContainer, rowHolders, item);
            }
        }

        Button btnAddExtraItem = new Button(context);
        btnAddExtraItem.setText("+ Add Another Variant ID to Order");
        btnAddExtraItem.setOnClickListener(v -> {
            SaleItemDto blank = new SaleItemDto(null, "Additional Variant Item", "", "", "", 1, 0.0, "");
            addEditableItemRow(itemsContainer, rowHolders, blank);
        });
        root.addView(btnAddExtraItem);

        new AlertDialog.Builder(context)
                .setTitle("Modify Order #" + order.getId())
                .setView(scrollView)
                .setPositiveButton("Save & Confirm Order", (d, w) ->
                        submitModifiedOrder(order.getId(), etAddress, etDiscount, etRemarks, rowHolders, true))
                .setNeutralButton("Save Changes Only", (d, w) ->
                        submitModifiedOrder(order.getId(), etAddress, etDiscount, etRemarks, rowHolders, false))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addEditableItemRow(LinearLayout container, List<ItemRowHolder> rowHolders, SaleItemDto item) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 16, 0, 16);

        TextView title = new TextView(context);
        String sizeColor = (item.getSize() != null ? item.getSize() : "") + " " + (item.getColor() != null ? item.getColor() : "");
        title.setText("• " + item.getProductName() + " (" + sizeColor.trim() + ")");
        title.setTypeface(null, Typeface.BOLD);
        row.addView(title);

        if (item.getItemNote() != null && !item.getItemNote().isEmpty()) {
            TextView tvNote = new TextView(context);
            tvNote.setText("  📝 Buyer Note: \"" + item.getItemNote() + "\"");
            row.addView(tvNote);
        }

        EditText etVarId = new EditText(context);
        etVarId.setHint("Variant ID *");
        etVarId.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (item.getVariantId() != null) {
            etVarId.setText(String.valueOf(item.getVariantId()));
        }
        row.addView(etVarId);

        LinearLayout qtyPriceRow = new LinearLayout(context);
        qtyPriceRow.setOrientation(LinearLayout.HORIZONTAL);

        EditText etQty = new EditText(context);
        etQty.setHint("Qty (0=Remove)");
        etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        etQty.setText(String.valueOf(item.getQuantity()));
        etQty.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        qtyPriceRow.addView(etQty);

        EditText etPrice = new EditText(context);
        etPrice.setHint("Unit Price (₹)");
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setText(String.valueOf(item.getPrice()));
        etPrice.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        qtyPriceRow.addView(etPrice);

        row.addView(qtyPriceRow);

        EditText etItemNote = new EditText(context);
        etItemNote.setHint("Item Note (Visible to Buyer)");
        etItemNote.setText(item.getItemNote() != null ? item.getItemNote() : "");
        row.addView(etItemNote);

        container.addView(row);
        rowHolders.add(new ItemRowHolder(etVarId, etQty, etPrice, etItemNote));
    }

    private void submitModifiedOrder(Long orderId, EditText etAddress, EditText etDiscount,
                                     EditText etRemarks, List<ItemRowHolder> rows, boolean confirmImmediately) {
        try {
            String address = etAddress.getText().toString().trim();
            String discountStr = etDiscount.getText().toString().trim();
            double discount = discountStr.isEmpty() ? 0.0 : Double.parseDouble(discountStr);
            String remarks = etRemarks.getText().toString().trim();

            List<SellerOrderUpdateRequestDto.OrderItemUpdateDto> updatedItems = new ArrayList<>();
            for (ItemRowHolder row : rows) {
                String varIdStr = row.etVariantId.getText().toString().trim();
                String qtyStr = row.etQty.getText().toString().trim();
                String priceStr = row.etPrice.getText().toString().trim();
                String note = row.etNote.getText().toString().trim();

                if (varIdStr.isEmpty() || qtyStr.isEmpty()) continue;
                long variantId = Long.parseLong(varIdStr);
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) continue;

                Double unitPrice = priceStr.isEmpty() ? null : Double.parseDouble(priceStr);
                updatedItems.add(new SellerOrderUpdateRequestDto.OrderItemUpdateDto(
                        variantId, qty, unitPrice, note.isEmpty() ? null : note));
            }

            if (updatedItems.isEmpty()) {
                Toast.makeText(context, "Order must have at least 1 item with Quantity > 0", Toast.LENGTH_LONG).show();
                return;
            }

            SellerOrderUpdateRequestDto request = new SellerOrderUpdateRequestDto(
                    address, discount, remarks, confirmImmediately, updatedItems
            );

            if (actionListener != null) {
                actionListener.onOrderModified(orderId, request);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Please enter valid numbers for Variant ID, Quantity, Price, and Discount.", Toast.LENGTH_LONG).show();
        }
    }

    private static class ItemRowHolder {
        final EditText etVariantId, etQty, etPrice, etNote;
        ItemRowHolder(EditText etVariantId, EditText etQty, EditText etPrice, EditText etNote) {
            this.etVariantId = etVariantId;
            this.etQty = etQty;
            this.etPrice = etPrice;
            this.etNote = etNote;
        }
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