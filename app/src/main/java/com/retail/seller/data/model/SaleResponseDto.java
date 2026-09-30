package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SaleResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("saleNumber")
    private String saleNumber;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("saleDate")
    private String saleDate;

    @SerializedName("totalAmount")
    private Double totalAmount;

    @SerializedName("items")
    private List<SaleItemDto> items;

    public SaleResponseDto() {
        this.items = new ArrayList<>();
    }

    public SaleResponseDto(Long id, String saleNumber, Long customerId, String customerName, String saleDate, Double totalAmount, List<SaleItemDto> items) {
        this.id = id;
        this.saleNumber = saleNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.items = items != null ? items : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public Double getTotalAmount() {
        return totalAmount != null ? totalAmount : 0.0;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SaleItemDto> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public void setItems(List<SaleItemDto> items) {
        this.items = items;
    }
}