package com.retail.seller.data.model;

import com.google.gson.annotations.SerializedName;

public class UserProfileDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("active")
    private Boolean active;

    @SerializedName("name")
    private String name;

    @SerializedName("businessName")
    private String businessName;

    @SerializedName("gstNumber")
    private String gstNumber;

    @SerializedName("businessPhone")
    private String businessPhone;

    @SerializedName("businessAddress")
    private String businessAddress;

    public UserProfileDto() {
    }

    public UserProfileDto(String name, String businessName, String gstNumber, String businessPhone, String businessAddress) {
        this.name = name;
        this.businessName = businessName;
        this.gstNumber = gstNumber;
        this.businessPhone = businessPhone;
        this.businessAddress = businessAddress;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role != null ? role : "BUYER"; }
    public Boolean getActive() { return active != null ? active : true; }
    public String getName() { return name; }
    public String getBusinessName() { return businessName; }
    public String getGstNumber() { return gstNumber; }
    public String getBusinessPhone() { return businessPhone; }
    public String getBusinessAddress() { return businessAddress; }
}