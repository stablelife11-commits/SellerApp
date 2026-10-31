package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class SellerOrderUpdateRequestDto {

    @SerializedName("deliveryAddress")
    private String deliveryAddress;

    @SerializedName("discountAmount")
    private Double discountAmount;

    @SerializedName("sellerRemarks")
    private String sellerRemarks;

    @SerializedName("confirmImmediately")
    private Boolean confirmImmediately;

    @SerializedName("items")
    private List<OrderItemUpdateDto> items;

    public SellerOrderUpdateRequestDto(String deliveryAddress, Double discountAmount, String sellerRemarks,
                                       Boolean confirmImmediately, List<OrderItemUpdateDto> items) {
        this.deliveryAddress = deliveryAddress;
        this.discountAmount = discountAmount != null ? discountAmount : 0.0;
        this.sellerRemarks = sellerRemarks;
        this.confirmImmediately = confirmImmediately;
        this.items = items != null ? items : new ArrayList<>();
    }

    public static class OrderItemUpdateDto {
        @SerializedName("variantId")
        private Long variantId;

        @SerializedName("quantity")
        private Integer quantity;

        @SerializedName("unitPrice")
        private Double unitPrice;

        @SerializedName("itemNote")
        private String itemNote;

        public OrderItemUpdateDto(Long variantId, Integer quantity, Double unitPrice, String itemNote) {
            this.variantId = variantId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.itemNote = itemNote;
        }
    }
}