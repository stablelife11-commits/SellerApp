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

    @SerializedName("size")
    private String size;

    @SerializedName("color")
    private String color;

    @SerializedName("sku")
    private String sku;

    @SerializedName("hsnCode")
    private String hsnCode;

    @SerializedName("gstPercentage")
    private Double gstPercentage;

    @SerializedName("taxAmount")
    private Double taxAmount;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName(value = "price", alternate = {"unitPrice"})
    private Double price;

    @SerializedName("itemNote")
    private String itemNote;

    public SaleItemDto() {
    }

    public SaleItemDto(Long variantId, String productName, String size, String color, String sku, Integer quantity, Double price, String itemNote) {
        this.variantId = variantId;
        this.productName = productName;
        this.size = size;
        this.color = color;
        this.sku = sku;
        this.quantity = quantity;
        this.price = price;
        this.itemNote = itemNote;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getVariantId() { return variantId; }
    public String getProductName() { return productName != null ? productName : "Unknown Product"; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public String getSku() { return sku; }
    public String getHsnCode() { return hsnCode; }
    public Double getGstPercentage() { return gstPercentage != null ? gstPercentage : 0.0; }
    public Double getTaxAmount() { return taxAmount != null ? taxAmount : 0.0; }
    public Integer getQuantity() { return quantity != null ? quantity : 0; }
    public Double getPrice() { return price != null ? price : 0.0; }
    public String getItemNote() { return itemNote; }
}