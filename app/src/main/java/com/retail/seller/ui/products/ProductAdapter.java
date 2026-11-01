package com.retail.seller.ui.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductResponseDto;
import com.retail.seller.data.model.ProductVariantDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductActionListener {
        void onUpdateStockClick(ProductResponseDto product);
        void onEditProductClick(ProductResponseDto product);
        void onToggleStatusClick(ProductResponseDto product);
    }

    private final Context context;
    private final List<ProductResponseDto> products;
    private final OnProductActionListener actionListener;

    public ProductAdapter(Context context, List<ProductResponseDto> products, OnProductActionListener actionListener) {
        this.context = context;
        this.products = products != null ? products : new ArrayList<>();
        this.actionListener = actionListener;
    }

    public void updateProducts(List<ProductResponseDto> newProducts) {
        this.products.clear();
        if (newProducts != null) {
            this.products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductResponseDto product = products.get(position);
        boolean isActive = Boolean.TRUE.equals(product.getActive());

        holder.tvProductName.setText(product.getName() != null ? product.getName() : "Unnamed Product");

        String brand = product.getBrand() != null && !product.getBrand().isEmpty() ? product.getBrand() : "Generic";
        String code = product.getProductCode() != null ? product.getProductCode() : "N/A";
        holder.tvProductSkuBrand.setText(String.format(Locale.getDefault(), "Style Code: %s • %s", code, brand));

        holder.tvCategoryTag.setText(product.getCategory() != null && !product.getCategory().isEmpty() ? product.getCategory() : "General");

        // Compute Wholesale Price Range & Total Stock
        int totalStock = 0;
        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0.0;
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (ProductVariantDto v : product.getVariants()) {
                totalStock += v.getStockQuantity();
                double p = v.getSellingPrice();
                if (p < minPrice) minPrice = p;
                if (p > maxPrice) maxPrice = p;
            }
        } else {
            minPrice = 0.0;
        }

        if (holder.tvWholesaleSummary != null) {
            String priceText = (Math.abs(maxPrice - minPrice) < 0.01)
                    ? String.format(Locale.getDefault(), "₹%.2f", minPrice)
                    : String.format(Locale.getDefault(), "₹%.0f - ₹%.0f", minPrice, maxPrice);
            holder.tvWholesaleSummary.setText(String.format(Locale.getDefault(),
                    "Wholesale: %s • Total Stock: %d", priceText, totalStock));
        }

        if (holder.tvProductStatusBadge != null) {
            if (isActive) {
                holder.tvProductStatusBadge.setText("ACTIVE");
                holder.tvProductStatusBadge.setBackgroundResource(R.drawable.bg_badge_emerald);
                holder.tvProductStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.emerald_dark));
            } else {
                holder.tvProductStatusBadge.setText("INACTIVE");
                holder.tvProductStatusBadge.setBackgroundResource(R.drawable.bg_badge_red);
                holder.tvProductStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning_red));
            }
        }

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()
                && product.getImageUrls().get(0) != null && !product.getImageUrls().get(0).trim().isEmpty()) {
            holder.ivProductThumb.setPadding(0, 0, 0, 0);
            Glide.with(context)
                    .load(product.getImageUrls().get(0).trim())
                    .placeholder(R.drawable.ic_box)
                    .error(R.drawable.ic_box)
                    .centerCrop()
                    .into(holder.ivProductThumb);
        } else {
            holder.ivProductThumb.setPadding(14, 14, 14, 14);
            holder.ivProductThumb.setImageResource(R.drawable.ic_box);
        }

        if (isActive) {
            holder.btnToggleStatus.setText("Deactivate");
            holder.btnToggleStatus.setTextColor(ContextCompat.getColor(context, R.color.warning_red));
        } else {
            holder.btnToggleStatus.setText("Activate");
            holder.btnToggleStatus.setTextColor(ContextCompat.getColor(context, R.color.emerald_dark));
        }

        holder.rvVariants.setLayoutManager(new LinearLayoutManager(context));
        VariantAdapter variantAdapter = new VariantAdapter(context, product.getVariants());
        holder.rvVariants.setAdapter(variantAdapter);

        holder.btnUpdateStock.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onUpdateStockClick(product);
        });

        holder.btnEditProduct.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEditProductClick(product);
        });

        holder.btnToggleStatus.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onToggleStatusClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductThumb;
        TextView tvProductName;
        TextView tvProductSkuBrand;
        TextView tvWholesaleSummary;
        TextView tvProductStatusBadge;
        TextView tvCategoryTag;
        RecyclerView rvVariants;
        Button btnUpdateStock;
        Button btnEditProduct;
        Button btnToggleStatus;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductThumb = itemView.findViewById(R.id.iv_product_thumb);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductSkuBrand = itemView.findViewById(R.id.tv_product_sku_brand);
            tvWholesaleSummary = itemView.findViewById(R.id.tv_product_wholesale_summary);
            tvProductStatusBadge = itemView.findViewById(R.id.tv_product_status_badge);
            tvCategoryTag = itemView.findViewById(R.id.tv_category_tag);
            rvVariants = itemView.findViewById(R.id.rv_variants);
            btnUpdateStock = itemView.findViewById(R.id.btn_update_stock);
            btnEditProduct = itemView.findViewById(R.id.btn_edit_product);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_product_status);
        }
    }
}