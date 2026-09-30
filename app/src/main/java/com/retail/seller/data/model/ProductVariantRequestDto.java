package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class ProductVariantRequestDto {

    @SerializedName("sku")
    private String sku;

    @SerializedName("size")
    private String size;

    @SerializedName("color")
    private String color;

    @SerializedName("sellingPrice")
    private Double sellingPrice;

    @SerializedName("purchasePrice")
    private Double purchasePrice;

    @SerializedName("stock")
    private Integer stock;

    public ProductVariantRequestDto() {
    }

    public ProductVariantRequestDto(String sku, String size, String color, Double sellingPrice, Double purchasePrice, Integer stock) {
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.sellingPrice = sellingPrice;
        this.purchasePrice = purchasePrice;
        this.stock = stock;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStockQuantity() {
        return getStock();
    }

    public void setStockQuantity(Integer stockQuantity) {
        setStock(stockQuantity);
    }
}