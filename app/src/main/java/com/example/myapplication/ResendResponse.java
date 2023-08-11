package com.example.myapplication;

public class ResendResponse {
    private int success;

    public ResendResponse(int success) {
        this.success = success;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }
}
