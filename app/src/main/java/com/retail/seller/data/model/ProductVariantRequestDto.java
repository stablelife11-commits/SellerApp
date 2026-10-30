package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class ProductVariantRequestDto {

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

    @SerializedName("minimumOrderQuantity")
    private Integer minimumOrderQuantity;

    @SerializedName("packSize")
    private Integer packSize;

    @SerializedName("hsnCode")
    private String hsnCode;

    @SerializedName("gstPercentage")
    private Double gstPercentage;

    public ProductVariantRequestDto() {
    }

    public ProductVariantRequestDto(String sku, String size, String color, Double sellingPrice, Double purchasePrice, Integer stock) {
        this(null, sku, size, color, sellingPrice, purchasePrice, stock, 1, 1, null, 0.0);
    }

    public ProductVariantRequestDto(Long id, String sku, String size, String color,
                                    Double sellingPrice, Double purchasePrice, Integer stock,
                                    Integer minimumOrderQuantity, Integer packSize,
                                    String hsnCode, Double gstPercentage) {
        this.id = id;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.sellingPrice = sellingPrice;
        this.purchasePrice = purchasePrice;
        this.stock = stock;
        this.minimumOrderQuantity = minimumOrderQuantity != null ? minimumOrderQuantity : 1;
        this.packSize = packSize != null ? packSize : 1;
        this.hsnCode = hsnCode;
        this.gstPercentage = gstPercentage != null ? gstPercentage : 0.0;
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

    public Integer getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }

    public void setMinimumOrderQuantity(Integer minimumOrderQuantity) {
        this.minimumOrderQuantity = minimumOrderQuantity;
    }

    public Integer getPackSize() {
        return packSize;
    }

    public void setPackSize(Integer packSize) {
        this.packSize = packSize;
    }

    public String getHsnCode() {
        return hsnCode;
    }

    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

    public Double getGstPercentage() {
        return gstPercentage;
    }

    public void setGstPercentage(Double gstPercentage) {
        this.gstPercentage = gstPercentage;
    }
}