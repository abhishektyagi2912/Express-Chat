package com.example.myapplication;


public class SignUpResponse {

    private int otp;

    private int success;

    private String message;

    public SignUpResponse(int otp, int success, String message) {
        this.otp = otp;
        this.success = success;
        this.message = message;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getOtp() {
        return otp;
    }
}
