package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SaleRequestDto {

    @SerializedName("saleNumber")
    private String saleNumber;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("saleDate")
    private String saleDate;

    @SerializedName("items")
    private List<SaleItemRequestDto> items;

    public SaleRequestDto() {
        this.items = new ArrayList<>();
    }

    public SaleRequestDto(String saleNumber, Long customerId, String saleDate, List<SaleItemRequestDto> items) {
        this.saleNumber = saleNumber;
        this.customerId = customerId;
        this.saleDate = saleDate;
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public List<SaleItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequestDto> items) {
        this.items = items;
    }
}