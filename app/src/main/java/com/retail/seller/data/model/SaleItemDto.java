package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class SaleItemDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("sku")
    private String sku;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("unitPrice")
    private Double unitPrice;

    @SerializedName("totalPrice")
    private Double totalPrice;

    public SaleItemDto() {
    }

    public SaleItemDto(Long id, Long variantId, String sku, Integer quantity, Double unitPrice, Double totalPrice) {
        this.id = id;
        this.variantId = variantId;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
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

    public Double getUnitPrice() {
        return unitPrice != null ? unitPrice : 0.0;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice != null ? totalPrice : 0.0;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getVariantName() {
        return getSku();
    }

    public Double getPrice() {
        return getUnitPrice();
    }
}