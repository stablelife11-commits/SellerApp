package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class InventoryTransactionDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("variantId")
    private Long variantId;

    @SerializedName("sku")
    private String sku;

    @SerializedName("productName")
    private String productName;

    @SerializedName("size")
    private String size;

    @SerializedName("color")
    private String color;

    @SerializedName("type")
    private String type;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("referenceType")
    private String referenceType;

    @SerializedName("referenceId")
    private Long referenceId;

    @SerializedName("previousStock")
    private Integer previousStock;

    @SerializedName("newStock")
    private Integer newStock;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() { return id; }
    public Long getVariantId() { return variantId; }
    public String getSku() { return sku != null ? sku : "N/A"; }
    public String getProductName() { return productName != null ? productName : "Product"; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public String getType() { return type != null ? type : "ADJUSTMENT"; }
    public Integer getQuantity() { return quantity != null ? quantity : 0; }
    public String getReferenceType() { return referenceType != null ? referenceType : "MANUAL"; }
    public Long getReferenceId() { return referenceId; }
    public Integer getPreviousStock() { return previousStock != null ? previousStock : 0; }
    public Integer getNewStock() { return newStock != null ? newStock : 0; }
    public String getCreatedAt() { return createdAt; }
}