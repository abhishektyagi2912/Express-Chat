package com.example.myapplication;

import android.net.wifi.hotspot2.pps.Credential;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("login") // Replace with your API endpoint
    Call<LoginResponse> login(@Body Map<String, String> requestBody);

    @POST("signup")
    Call<SignUpResponse> signup(@Body Map<String, String> signupBody);

    // for verification
    @GET("verify")
    Call<VerifyResponse> verifyOTP(
            @Query("email") String email,
            @Query("token") String token,
            @Query("source") String source
    );

    // for resend
    @POST("resend")
    Call<ResendResponse> resendOTP(@Body Map<String, String> resendOTP);
}
