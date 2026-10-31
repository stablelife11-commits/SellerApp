package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SaleResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("saleNumber")
    private String saleNumber;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("customerMobile")
    private String customerMobile;

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "saleDate", alternate = {"orderDate"})
    private String saleDate;

    @SerializedName("subtotalAmount")
    private Double subtotalAmount;

    @SerializedName("discountAmount")
    private Double discountAmount;

    @SerializedName("totalAmount")
    private Double totalAmount;

    @SerializedName("paidAmount")
    private Double paidAmount;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("sellerRemarks")
    private String sellerRemarks;

    @SerializedName("items")
    private List<SaleItemDto> items;

    public SaleResponseDto() {
        this.items = new ArrayList<>();
    }

    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public String getSaleNumber() { return saleNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerMobile() { return customerMobile; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getStatus() { return status != null ? status : "PLACED"; }
    public String getSaleDate() { return saleDate; }
    public Double getSubtotalAmount() { return subtotalAmount != null ? subtotalAmount : getTotalAmount(); }
    public Double getDiscountAmount() { return discountAmount != null ? discountAmount : 0.0; }
    public Double getTotalAmount() { return totalAmount != null ? totalAmount : 0.0; }
    public Double getPaidAmount() { return paidAmount != null ? paidAmount : 0.0; }
    public String getPaymentStatus() { return paymentStatus != null ? paymentStatus : "UNPAID"; }
    public String getSellerRemarks() { return sellerRemarks; }
    public List<SaleItemDto> getItems() { return items != null ? items : new ArrayList<>(); }
}