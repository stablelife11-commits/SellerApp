package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class ReturnItemRequestDto {

    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("quantity")
    private Integer quantity;

    public ReturnItemRequestDto() {
    }

    public ReturnItemRequestDto(Long variantId, Integer quantity) {
        this.variantId = variantId;
        this.quantity = quantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}