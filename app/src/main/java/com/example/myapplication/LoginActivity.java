package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

//    private static final String REALM_APP_ID = "bhart_chat-whcgp";
//    private static final String REALM_APP_NAME = "Bhart_Chat";

    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView HaveAlready;
    private SharedPreferences sharedPreferences;

   private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        HaveAlready = findViewById(R.id.HaveAlready);
        editTextEmail = findViewById(R.id.SignUpEmailId);
        editTextPassword = findViewById(R.id.SignUpPassword);

        // shared pref
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("accessToken", null);
        String refreshToken = sharedPreferences.getString("refreshToken", null);
        if (accessToken != null && refreshToken != null) {
            startMainActivity();
        }

        View rootLayout = findViewById(R.id.loginRoot);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (editTextEmail.isFocused() ||  editTextPassword.isFocused()) {
                        Rect outRect = new Rect();
                        editTextEmail.getGlobalVisibleRect(outRect);
                        editTextPassword.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            // Hide the soft keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);
                            imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);
                            // Remove focus from the emailEditText
                            editTextEmail.clearFocus();
                            editTextPassword.clearFocus();
                        }
                    }
                }
                return false;
            }
        });

        // redirect to signup page
        HaveAlready.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this,SignActivity.class));
            finish();
        });

        /// Api link retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://expresschat-v6mg.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // api interface of this
        apiService = retrofit.create(ApiService.class);

        // No user is logged in, proceed to login or sign-up
        // For this example, let's assume you have a button to start the login process
        Button loginButton = findViewById(R.id.SignUpBtn);
        loginButton.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String source = "app";
            loginUser(email,password,source);
        });
    }

    private void loginUser(String email, String password, String source) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("source", source);

        // Make the API request
        Call<LoginResponse> call = apiService.login(requestBody);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    handleLoginResponse(response,email);
                } else {
                    showErrorToast("Login Failed");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                showErrorToast("Error: " + t.getMessage());
            }
        });
    }

    private void handleLoginResponse(Response<LoginResponse> response,String email) {
        if (response.isSuccessful()) {
            List<String> cookies = response.headers().values("Set-Cookie");
            if (cookies != null && !cookies.isEmpty()) {
                String accessToken = null;
                String refreshToken = null;
                for (String cookie : cookies) {
                    if (cookie.contains("access_token")) {
                        accessToken = extractTokenFromCookie(cookie);
                    } else if (cookie.contains("refresh_token")) {
                        refreshToken = extractTokenFromCookie(cookie);
                    }
                }

                if (accessToken != null && refreshToken != null) {
                    saveTokensToSharedPreferences(accessToken, refreshToken);
                    // Continue with successful login flow
                    LoginResponse loginResponse = response.body();
                    if (loginResponse != null && loginResponse.getSuccess() == 1) {
                        String message = loginResponse.getMessage();
                        showSuccessToast(message);
                        startMainActivity();
                    } else if(loginResponse.getSuccess() == -2){
                        Toast.makeText(this, "Please verify your account ", Toast.LENGTH_SHORT).show();
                        resendOTP(email,"app");
                        startActivity(new Intent(LoginActivity.this, VerifyActivity.class));
                    }
                } else {
                    showErrorToast("Login Failed");
                }
            } else {
                showErrorToast("Login Failed");
            }
        } else {
            showErrorToast("Login Failed");
        }
    }

    private void resendOTP(String email,String app){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://expresschat-v6mg.onrender.com/")
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
                Toast.makeText(LoginActivity.this, "Please press resend again", Toast.LENGTH_SHORT).show();
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


    private String extractTokenFromCookie(String cookie) {
        String[] parts = cookie.split(";");
        return parts[0].substring(parts[0].indexOf("=") + 1);
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

    private boolean isValidEmail(String email) {
        // Implement your email validation logic here
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        // Implement your password validation logic here
        return password.length() >= 6; // For example, password must be at least 6 characters long
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LoginActivity.this,StartActivity.class));
        finish();
    }
}