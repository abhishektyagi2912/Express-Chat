package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VerifyActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        Button validate = findViewById(R.id.ValidateOtp);
        TextView resendTextView = findViewById(R.id.resendTextView);
//        int otp = getIntent().getIntExtra("otp", 0);
        String email = getIntent().getStringExtra("email");

        EditText editTextOTP1 = findViewById(R.id.editTextOTP1);
        EditText editTextOTP2 = findViewById(R.id.editTextOTP2);
        EditText editTextOTP3 = findViewById(R.id.editTextOTP3);
        EditText editTextOTP4 = findViewById(R.id.editTextOTP4);
        EditText editTextOTP5 = findViewById(R.id.editTextOTP5);
        EditText editTextOTP6 = findViewById(R.id.editTextOTP6);

        editTextOTP1.addTextChangedListener(new OTPTextWatcher(editTextOTP1, editTextOTP2));
        editTextOTP2.addTextChangedListener(new OTPTextWatcher(editTextOTP2, editTextOTP3));
        editTextOTP3.addTextChangedListener(new OTPTextWatcher(editTextOTP3, editTextOTP4));
        editTextOTP4.addTextChangedListener(new OTPTextWatcher(editTextOTP4, editTextOTP5));
        editTextOTP5.addTextChangedListener(new OTPTextWatcher(editTextOTP5, editTextOTP6));

        validate.setOnClickListener(v -> {
            String otp1 = editTextOTP1.getText().toString();
            String otp2 = editTextOTP2.getText().toString();
            String otp3 = editTextOTP3.getText().toString();
            String otp4 = editTextOTP4.getText().toString();
            String otp5 = editTextOTP5.getText().toString();
            String otp6 = editTextOTP6.getText().toString();

            String combinedOTP = otp1 + otp2 + otp3 + otp4 + otp5 + otp6;
            int ans = Integer.parseInt(combinedOTP);
            verifyOTPWithServer(email, String.valueOf(ans));
        });

        resendTextView.setOnClickListener(v -> resendOTP(email,"app"));
    }
    // this will work if user come to the verify page first
    private void verifyOTPWithServer(String email, String otp) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://expresschat-v6mg.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<VerifyResponse> call = apiService.verifyOTP(email, otp, "app");
        call.enqueue(new Callback<VerifyResponse>() {
            @Override
            public void onResponse(Call<VerifyResponse> call, Response<VerifyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginResponse(response);
                } else {
                    Toast.makeText(VerifyActivity.this, "Failed to verify OTP.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerifyResponse> call, Throwable t) {
                Toast.makeText(VerifyActivity.this, "Some Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void handleLoginResponse(Response<VerifyResponse> response) {
        if (response.isSuccessful()) {
            List<String> cookies = response.headers().values("Set-Cookie");
            if (cookies != null && !cookies.isEmpty()) {
                String accessToken = null;
                String refreshToken = null;
                for (String cookie : cookies) {
                    if (cookie.contains("access_token")) {
                        accessToken = extractTokenFromCookie(cookie, "access_token");
                    } else if (cookie.contains("refresh_token")) {
                        refreshToken = extractTokenFromCookie(cookie, "refresh_token");
                    }
                }

                if (accessToken != null && refreshToken != null) {
                    saveTokensToSharedPreferences(accessToken, refreshToken);
                    // Continue with successful login flow
                    VerifyResponse loginResponse = response.body();
                    if (loginResponse != null && loginResponse.getSuccess() == 1) {
//                        String message = loginResponse.getMessage();
                        // here toast are changed
                        String message = "Login successfully";
                        showSuccessToast(message);
                        startMainActivity();
                    } else if (loginResponse.getSuccess() == 0) {
                        Toast.makeText(this, "OTP is expired", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showErrorToast("Validate Failed");
                }
            } else {
                showErrorToast("Validate Failed");
            }
        } else {
            showErrorToast("Validate Failed");
        }
    }


    private void resendOTP(String email,String app){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://bharatchat.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email",email);
        requestBody.put("source",app);
        Call<ResendResponse> call = apiService.resendOTP(requestBody);
        call.enqueue(new Callback<ResendResponse>() {
            @Override
            public void onResponse(Call<ResendResponse> call, Response<ResendResponse> response) {
                if(response.isSuccessful()){
                    ResendResponse resendResponse = response.body();
                    handleResendResponse(resendResponse);
                }
            }
            @Override
            public void onFailure(Call<ResendResponse> call, Throwable t) {
                Toast.makeText(VerifyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleResendResponse(ResendResponse resendResponse){
        if(resendResponse == null){
            Toast.makeText(this, "Some error occurred, Please try again later", Toast.LENGTH_SHORT).show();
        }else {
            if(resendResponse.getSuccess() == -1){
                Toast.makeText(this, "Not a valid email or some error plz try again", Toast.LENGTH_SHORT).show();
            } else if (resendResponse.getSuccess() == 1) {
                Toast.makeText(this, "Successfully send an email", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String extractTokenFromCookie(String cookie, String tokenType) {
        String[] parts = cookie.split(";");
        for (String part : parts) {
            if (part.contains(tokenType)) {
                return part.substring(part.indexOf("=") + 1);
            }
        }
        return null;
    }

    private void showSuccessToast(String message) {
        Toast.makeText(this, "Success: " + message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
    }

    private void saveTokensToSharedPreferences(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accessToken", accessToken);
        editor.putString("refreshToken", refreshToken);
        editor.apply();
    }
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private static class OTPTextWatcher implements TextWatcher {
        private EditText currentEditText;
        private EditText nextEditText;

        public OTPTextWatcher(EditText currentEditText, EditText nextEditText) {
            this.currentEditText = currentEditText;
            this.nextEditText = nextEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1) {
                // Automatically move focus to the next EditText
                nextEditText.requestFocus();
            }
        }
    }

}