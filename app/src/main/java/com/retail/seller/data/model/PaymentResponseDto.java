package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("amount")
    private Double amount;

    @SerializedName("paymentDate")
    private String paymentDate;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("referenceType")
    private String referenceType;

    @SerializedName("referenceId")
    private Long referenceId;

    @SerializedName("notes")
    private String notes;

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public Double getAmount() { return amount != null ? amount : 0.0; }
    public String getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getReferenceType() { return referenceType; }
    public Long getReferenceId() { return referenceId; }
    public String getNotes() { return notes; }
}