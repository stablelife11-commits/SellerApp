package com.retail.seller.ui.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.retail.seller.R;
import com.retail.seller.data.model.ProductResponseDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnUpdateStockClickListener {
        void onUpdateStockClick(ProductResponseDto product);
    }

    private final Context context;
    private final List<ProductResponseDto> products;
    private final OnUpdateStockClickListener updateStockClickListener;

    public ProductAdapter(Context context, List<ProductResponseDto> products, OnUpdateStockClickListener updateStockClickListener) {
        this.context = context;
        this.products = products != null ? products : new ArrayList<>();
        this.updateStockClickListener = updateStockClickListener;
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

        holder.tvProductName.setText(product.getName() != null ? product.getName() : "Unnamed Product");

        String brand = product.getBrand() != null ? product.getBrand() : "Generic";
        String sku = product.getProductCode() != null ? product.getProductCode() : "N/A";
        holder.tvProductSkuBrand.setText(String.format(Locale.getDefault(), "SKU: %s | Brand: %s", sku, brand));

        holder.tvCategoryTag.setText(product.getCategory() != null && !product.getCategory().isEmpty() ? product.getCategory() : "General");

        // Set up inner RecyclerView for product variants
        holder.rvVariants.setLayoutManager(new LinearLayoutManager(context));
        VariantAdapter variantAdapter = new VariantAdapter(context, product.getVariants());
        holder.rvVariants.setAdapter(variantAdapter);

        // Update Stock button listener
        holder.btnUpdateStock.setOnClickListener(v -> {
            if (updateStockClickListener != null) {
                updateStockClickListener.onUpdateStockClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvProductSkuBrand;
        TextView tvCategoryTag;
        RecyclerView rvVariants;
        Button btnUpdateStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductSkuBrand = itemView.findViewById(R.id.tv_product_sku_brand);
            tvCategoryTag = itemView.findViewById(R.id.tv_category_tag);
            rvVariants = itemView.findViewById(R.id.rv_variants);
            btnUpdateStock = itemView.findViewById(R.id.btn_update_stock);
        }
    }
}