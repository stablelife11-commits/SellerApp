package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponseDto {

    @SerializedName("token")
    private String token;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("email")
    private String email;

    @SerializedName("name")
    private String name;

    @SerializedName("role")
    private String role;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("businessName")
    private String businessName;

    @SerializedName("gstNumber")
    private String gstNumber;

    @SerializedName("businessPhone")
    private String businessPhone;

    @SerializedName("businessAddress")
    private String businessAddress;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, Long userId, String email, String name, String role, Long customerId) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.customerId = customerId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getBusinessPhone() { return businessPhone; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }

    public String getBusinessAddress() { return businessAddress; }
    public void setBusinessAddress(String businessAddress) { this.businessAddress = businessAddress; }
}