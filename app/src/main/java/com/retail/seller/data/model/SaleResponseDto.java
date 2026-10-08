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

    // 🟢 NAYA: Customer ka Mobile Number (Invoice ke liye)
    @SerializedName("customerMobile")
    private String customerMobile;

    // 🟢 NAYA: Delivery Address (Invoice ke liye)
    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    // 🟢 NAYA: Order ka Status (PLACED / CONFIRMED)
    @SerializedName("status")
    private String status;

    // 🟢 FIX: Backend ab 'orderDate' bhejta hai, toh 'alternate' lagana zaruri hai
    @SerializedName(value = "saleDate", alternate = {"orderDate"})
    private String saleDate;

    @SerializedName("totalAmount")
    private Double totalAmount;

    @SerializedName("items")
    private List<SaleItemDto> items;

    public SaleResponseDto() {
        this.items = new ArrayList<>();
    }

    public Long getId() { return id; }
    public String getSaleNumber() { return saleNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }

    public String getCustomerMobile() { return customerMobile; } // 🟢 Naya Getter
    public String getDeliveryAddress() { return deliveryAddress; } // 🟢 Naya Getter
    public String getStatus() { return status; } // 🟢 Naya Getter

    public String getSaleDate() { return saleDate; }
    public Double getTotalAmount() { return totalAmount != null ? totalAmount : 0.0; }
    public List<SaleItemDto> getItems() { return items != null ? items : new ArrayList<>(); }
}