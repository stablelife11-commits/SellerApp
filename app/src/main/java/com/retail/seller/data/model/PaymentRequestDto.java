package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentRequestDto {

    @SerializedName("customerId")
    private Long customerId;

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

    public PaymentRequestDto() {
    }

    public PaymentRequestDto(Long customerId, Double amount, String paymentDate,
                             String paymentMethod, String referenceType, Long referenceId, String notes) {
        this.customerId = customerId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.notes = notes;
    }

    public Long getCustomerId() { return customerId; }
    public Double getAmount() { return amount; }
    public String getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getReferenceType() { return referenceType; }
    public Long getReferenceId() { return referenceId; }
    public String getNotes() { return notes; }
}