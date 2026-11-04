package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class RegisterSellerRequestDto {

    @SerializedName("name")
    private final String name;

    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    @SerializedName("mobile")
    private final String mobile;

    @SerializedName("role")
    private final String role = "SELLER";

    @SerializedName("businessName")
    private final String businessName;

    @SerializedName("gstNumber")
    private final String gstNumber;

    @SerializedName("businessAddress")
    private final String businessAddress;

    public RegisterSellerRequestDto(String name, String email, String password, String mobile,
                                    String businessName, String gstNumber, String businessAddress) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
        this.businessName = businessName;
        this.gstNumber = gstNumber;
        this.businessAddress = businessAddress;
    }
}