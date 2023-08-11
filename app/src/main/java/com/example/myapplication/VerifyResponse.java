package com.example.myapplication;
import com.google.gson.annotations.SerializedName;

public class VerifyResponse {

    @SerializedName("success")
    private int success;

    @SerializedName("message")
    private String message;
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;


    public VerifyResponse(String accessToken, String refreshToken, int success, String message) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.success = success;
        this.message = message;
    }
    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
