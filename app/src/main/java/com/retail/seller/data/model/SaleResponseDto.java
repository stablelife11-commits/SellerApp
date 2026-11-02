package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SaleResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("sellerId")
    private Long sellerId;

    @SerializedName("sellerBusinessName")
    private String sellerBusinessName;

    @SerializedName("sellerGstNumber")
    private String sellerGstNumber;

    @SerializedName("sellerPhone")
    private String sellerPhone;

    @SerializedName("sellerAddress")
    private String sellerAddress;

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

    @SerializedName("taxAmount")
    private Double taxAmount;

    @SerializedName("totalAmount")
    private Double totalAmount;

    @SerializedName("returnedAmount")
    private Double returnedAmount;

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
    public String getSellerBusinessName() { return sellerBusinessName; }
    public String getSellerGstNumber() { return sellerGstNumber; }
    public String getSellerPhone() { return sellerPhone; }
    public String getSellerAddress() { return sellerAddress; }
    public String getSaleNumber() { return saleNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerMobile() { return customerMobile; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getStatus() { return status != null ? status : "PLACED"; }
    public String getSaleDate() { return saleDate; }
    public Double getSubtotalAmount() { return subtotalAmount != null ? subtotalAmount : getTotalAmount(); }
    public Double getDiscountAmount() { return discountAmount != null ? discountAmount : 0.0; }
    public Double getTaxAmount() { return taxAmount != null ? taxAmount : 0.0; }
    public Double getTotalAmount() { return totalAmount != null ? totalAmount : 0.0; }
    public Double getReturnedAmount() { return returnedAmount != null ? returnedAmount : 0.0; }
    public Double getPaidAmount() { return paidAmount != null ? paidAmount : 0.0; }
    public String getPaymentStatus() { return paymentStatus != null ? paymentStatus : "UNPAID"; }
    public String getSellerRemarks() { return sellerRemarks; }
    public List<SaleItemDto> getItems() { return items != null ? items : new ArrayList<>(); }

    public double getNetPayableAmount() {
        return Math.max(0.0, getTotalAmount() - getReturnedAmount());
    }

    public double getBalanceDueAmount() {
        return Math.max(0.0, getNetPayableAmount() - getPaidAmount());
    }

    public double getRefundCreditAmount() {
        return Math.max(0.0, getPaidAmount() - getNetPayableAmount());
    }
}