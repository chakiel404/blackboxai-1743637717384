package com.smartapp.models;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("email")
    private String identifier;

    @SerializedName("password")
    private String password;

    @SerializedName("login_type")
    private String loginType;

    public LoginRequest() {
    }

    public LoginRequest(String identifier, String password, String loginType) {
        this.identifier = identifier;
        this.password = password;
        this.loginType = loginType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setEmail(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}