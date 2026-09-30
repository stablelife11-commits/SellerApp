package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class ProductVariantDto {

    @SerializedName("id")
    private Long id;

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

    public ProductVariantDto() {
    }

    public ProductVariantDto(Long id, String sku, String size, String color, Double sellingPrice, Double purchasePrice, Integer stock) {
        this.id = id;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.sellingPrice = sellingPrice;
        this.purchasePrice = purchasePrice;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return sellingPrice != null ? sellingPrice : 0.0;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Double getPurchasePrice() {
        return purchasePrice != null ? purchasePrice : 0.0;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getStock() {
        return stock != null ? stock : 0;
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