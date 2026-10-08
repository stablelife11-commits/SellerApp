package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class SaleItemDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("productName")
    private String productName;

    // 🟢 NAYA: Backend se Size aur Color nikalna
    @SerializedName("size")
    private String size;

    @SerializedName("color")
    private String color;

    @SerializedName("sku")
    private String sku;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName(value = "price", alternate = {"unitPrice"})
    private Double price;

    public Long getId() { return id; }
    public Long getVariantId() { return variantId; }
    public String getProductName() { return productName != null ? productName : "Unknown Product"; }
    public String getSize() { return size; } // Getter
    public String getColor() { return color; } // Getter
    public String getSku() { return sku; }
    public Integer getQuantity() { return quantity != null ? quantity : 0; }
    public Double getPrice() { return price != null ? price : 0.0; }
}