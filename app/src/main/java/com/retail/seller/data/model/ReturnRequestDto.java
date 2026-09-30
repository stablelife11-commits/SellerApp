package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ReturnRequestDto {

    @SerializedName("saleId")
    private Long saleId;

    @SerializedName("reason")
    private String reason;

    @SerializedName("items")
    private List<ReturnItemRequestDto> items;

    public ReturnRequestDto() {
        this.items = new ArrayList<>();
    }

    public ReturnRequestDto(Long saleId, String reason, List<ReturnItemRequestDto> items) {
        this.saleId = saleId;
        this.reason = reason;
        this.items = items != null ? items : new ArrayList<>();
    }

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<ReturnItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<ReturnItemRequestDto> items) {
        this.items = items;
    }
}