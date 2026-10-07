package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class SaleItemDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("productId")
    private Long productId;

    @SerializedName("variantId")
    private Long variantId;

    // 🟢 NAYA: Backend se aane wala asli product name
    @SerializedName("productName")
    private String productName;

    @SerializedName("sku")
    private String sku;

    @SerializedName("quantity")
    private Integer quantity;

    // 🟢 NAYA: 'alternate' lagaya taaki agar backend 'price' bheje toh bhi pakad le
    @SerializedName(value = "price", alternate = {"unitPrice"})
    private Double price;

    @SerializedName("totalPrice")
    private Double totalPrice;

    public SaleItemDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity != null ? quantity : 0;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // 🟢 NAYA: 'boolean' se badal kar 'String' kar diya
    public String getProductName() {
        if (productName != null && !productName.isEmpty()) {
            return productName;
        }
        return sku != null ? sku : "Unknown Product";
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price != null ? price : 0.0;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getTotalPrice() {
        return totalPrice != null ? totalPrice : 0.0;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}