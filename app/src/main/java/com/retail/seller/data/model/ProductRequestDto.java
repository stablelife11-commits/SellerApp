package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ProductRequestDto {

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("productCode")
    private String productCode;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("brand")
    private String brand;

    @SerializedName("variants")
    private List<ProductVariantRequestDto> variants;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    public ProductRequestDto() {
        this.variants = new ArrayList<>();
        this.imageUrls = new ArrayList<>();
    }

    public ProductRequestDto(Long sellerId, String productCode, String name, String description, String category, String brand, List<ProductVariantRequestDto> variants, List<String> imageUrls) {
        this.sellerId = sellerId;
        this.productCode = productCode;
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.variants = variants != null ? variants : new ArrayList<>();
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public List<ProductVariantRequestDto> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantRequestDto> variants) {
        this.variants = variants;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}