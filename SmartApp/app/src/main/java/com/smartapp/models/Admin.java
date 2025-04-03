package com.smartapp.models;

import com.google.gson.annotations.SerializedName;

public class Admin {
    @SerializedName("admin_id")
    private int adminId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("admin_code")
    private String adminCode;

    @SerializedName("gender")
    private String gender;

    @SerializedName("birth_date")
    private String birthDate;

    @SerializedName("birth_place")
    private String birthPlace;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    // Basic info from User table
    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}