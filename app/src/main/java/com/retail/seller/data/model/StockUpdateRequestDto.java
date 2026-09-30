package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class StockUpdateRequestDto {

    @SerializedName("stock")
    private Integer stock;

    public StockUpdateRequestDto() {
    }

    public StockUpdateRequestDto(Integer stock) {
        this.stock = stock;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}