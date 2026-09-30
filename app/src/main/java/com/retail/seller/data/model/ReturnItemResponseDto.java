package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class ReturnItemResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("sku")
    private String sku;

    @SerializedName("quantity")
    private Integer quantity;

    public ReturnItemResponseDto() {
    }

    public ReturnItemResponseDto(Long id, Long variantId, String sku, Integer quantity) {
        this.id = id;
        this.variantId = variantId;
        this.sku = sku;
        this.quantity = quantity;
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
}