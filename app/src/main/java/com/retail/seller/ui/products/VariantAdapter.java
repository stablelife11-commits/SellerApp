package com.retail.seller.ui.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductVariantDto;
import java.util.List;
import java.util.Locale;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.VariantViewHolder> {

    private final Context context;
    private final List<ProductVariantDto> variants;

    public VariantAdapter(Context context, List<ProductVariantDto> variants) {
        this.context = context;
        this.variants = variants;
    }

    @NonNull
    @Override
    public VariantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_variant, parent, false);
        return new VariantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VariantViewHolder holder, int position) {
        ProductVariantDto variant = variants.get(position);

        String attrText = String.format(Locale.getDefault(), "%s | %s",
                variant.getSize() != null && !variant.getSize().isEmpty() ? "Size: " + variant.getSize() : "Variant",
                variant.getColor() != null && !variant.getColor().isEmpty() ? variant.getColor() : "Standard");
        holder.tvVariantAttr.setText(attrText);

        holder.tvVariantPrice.setText(String.format(Locale.getDefault(), "$%.2f", variant.getSellingPrice()));

        int stock = variant.getStockQuantity();
        holder.tvVariantStock.setText(String.format(Locale.getDefault(), "Stock: %d", stock));

        if (stock < 10) {
            holder.tvVariantStock.setBackgroundResource(R.drawable.bg_badge_red);
            holder.tvVariantStock.setTextColor(ContextCompat.getColor(context, R.color.warning_red));
        } else {
            holder.tvVariantStock.setBackgroundResource(R.drawable.bg_badge_emerald);
            holder.tvVariantStock.setTextColor(ContextCompat.getColor(context, R.color.emerald_dark));
        }
    }

    @Override
    public int getItemCount() {
        return variants != null ? variants.size() : 0;
    }

    static class VariantViewHolder extends RecyclerView.ViewHolder {
        TextView tvVariantAttr;
        TextView tvVariantPrice;
        TextView tvVariantStock;

        public VariantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVariantAttr = itemView.findViewById(R.id.tv_variant_attr);
            tvVariantPrice = itemView.findViewById(R.id.tv_variant_price);
            tvVariantStock = itemView.findViewById(R.id.tv_variant_stock);
        }
    }
}