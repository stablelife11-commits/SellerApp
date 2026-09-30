package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ReturnResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("saleId")
    private Long saleId;

    @SerializedName("returnDate")
    private String returnDate;

    @SerializedName("reason")
    private String reason;

    @SerializedName("items")
    private List<ReturnItemResponseDto> items;

    public ReturnResponseDto() {
        this.items = new ArrayList<>();
    }

    public ReturnResponseDto(Long id, Long saleId, String returnDate, String reason, List<ReturnItemResponseDto> items) {
        this.id = id;
        this.saleId = saleId;
        this.returnDate = returnDate;
        this.reason = reason;
        this.items = items != null ? items : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<ReturnItemResponseDto> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public void setItems(List<ReturnItemResponseDto> items) {
        this.items = items;
    }

    // Helper methods for UI compatibility
    public Integer getQuantityRestored() {
        int total = 0;
        if (items != null) {
            for (ReturnItemResponseDto item : items) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    public String getStatus() {
        return "PROCESSED";
    }

    public Double getRefundAmount() {
        return 0.0;
    }
}