package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ProductResponseDto {

    @SerializedName("id")
    private Long id;

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

    @SerializedName("active")
    private Boolean active;

    @SerializedName("variants")
    private List<ProductVariantDto> variants;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    public ProductResponseDto() {
        this.variants = new ArrayList<>();
        this.imageUrls = new ArrayList<>();
    }

    public ProductResponseDto(Long id, Long sellerId, String productCode, String name, String description, String category, String brand, Boolean active, List<ProductVariantDto> variants, List<String> imageUrls) {
        this.id = id;
        this.sellerId = sellerId;
        this.productCode = productCode;
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.active = active;
        this.variants = variants != null ? variants : new ArrayList<>();
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<ProductVariantDto> getVariants() {
        return variants != null ? variants : new ArrayList<>();
    }

    public void setVariants(List<ProductVariantDto> variants) {
        this.variants = variants;
    }

    public List<String> getImageUrls() {
        return imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}