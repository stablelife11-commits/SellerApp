package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class DashboardSummaryDto {

    @SerializedName("totalSalesAmount")
    private Double totalSalesAmount;

    @SerializedName("posSalesAmount")
    private Double posSalesAmount;

    @SerializedName("onlineOrderSalesAmount")
    private Double onlineOrderSalesAmount;

    @SerializedName("totalReturnsAmount")
    private Double totalReturnsAmount;

    @SerializedName("totalPurchaseAmount")
    private Double totalPurchaseAmount;

    @SerializedName("totalExpensesAmount")
    private Double totalExpensesAmount;

    @SerializedName("totalCustomers")
    private Long totalCustomers;

    @SerializedName("totalProducts")
    private Long totalProducts;

    @SerializedName("totalSuppliers")
    private Long totalSuppliers;

    public DashboardSummaryDto() {
    }

    public Double getTotalSalesAmount() {
        return totalSalesAmount != null ? totalSalesAmount : 0.0;
    }

    public Double getPosSalesAmount() {
        return posSalesAmount != null ? posSalesAmount : 0.0;
    }

    public Double getOnlineOrderSalesAmount() {
        return onlineOrderSalesAmount != null ? onlineOrderSalesAmount : 0.0;
    }

    public Double getTotalReturnsAmount() {
        return totalReturnsAmount != null ? totalReturnsAmount : 0.0;
    }

    public Double getTotalPurchaseAmount() {
        return totalPurchaseAmount != null ? totalPurchaseAmount : 0.0;
    }

    public Double getTotalExpensesAmount() {
        return totalExpensesAmount != null ? totalExpensesAmount : 0.0;
    }

    public Long getTotalCustomers() {
        return totalCustomers != null ? totalCustomers : 0L;
    }

    public Long getTotalProducts() {
        return totalProducts != null ? totalProducts : 0L;
    }

    public Long getTotalSuppliers() {
        return totalSuppliers != null ? totalSuppliers : 0L;
    }
}