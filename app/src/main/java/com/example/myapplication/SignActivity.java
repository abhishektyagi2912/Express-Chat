package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignActivity extends AppCompatActivity {

    TextView NotAlready;
    private ApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        Button signupButton = findViewById(R.id.SignInBtn);
        EditText nameEditText = findViewById(R.id.SignInName);
        EditText emailEditText = findViewById(R.id.SignInEmailId);
        EditText passwordEditText = findViewById(R.id.SignInPassword);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://bharatchat.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // this is automatically back listener
        View rootLayout = findViewById(R.id.rootLayout);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (emailEditText.isFocused() || nameEditText.isFocused() || passwordEditText.isFocused()) {
                        Rect outRect = new Rect();
                        emailEditText.getGlobalVisibleRect(outRect);
                        nameEditText.getGlobalVisibleRect(outRect);
                        passwordEditText.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            // Hide the soft keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
                            imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
                            imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
                            // Remove focus from the emailEditText
                            emailEditText.clearFocus();
                            nameEditText.clearFocus();
                            passwordEditText.clearFocus();
                        }
                    }
                }
                return false;
            }
        });


        NotAlready = findViewById(R.id.NotAlready);
        NotAlready.setOnClickListener(v -> {
            startActivity(new Intent(SignActivity.this,LoginActivity.class));
            finish();
        });

        signupButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String source = "app";
            signupUser(name,email,password,source);
        });
    }

    private void signupUser(String name, String email, String password, String source){
        ///// map
        Map<String, String> signupBody = new HashMap<>();
        signupBody.put("name", name);
        signupBody.put("email", email);
        signupBody.put("password", password);
        signupBody.put("source", source);
        Call<SignUpResponse> call = apiService.signup(signupBody);
        call.enqueue(new Callback<SignUpResponse>() {
            @Override
            public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
                if (response.isSuccessful() ) {
                    SignUpResponse signUpResponse = response.body();
                    handleSignupResponse(signUpResponse,email);
                } else {
                    Toast.makeText(SignActivity.this, "User Already Exist", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(SignActivity.this, "Error " + apiResponse, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SignUpResponse> call, Throwable t) {
                Toast.makeText(SignActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void handleSignupResponse(SignUpResponse apiResponse, String email) {
        if (apiResponse == null) {
            // API error occurred or response parsing failed, display error message
            Toast.makeText(this, "Error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
        } else {
            if (apiResponse.getSuccess() == 0) {
                // User already exists, display toast message
                Toast.makeText(this, "Already Exist", Toast.LENGTH_SHORT).show();
            } else if (apiResponse.getSuccess() == 1) {
                // Signup successful, redirect to verification page
//                int generatedOTP = apiResponse.getOtp();
                Toast.makeText(this, "Otp sent successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignActivity.this, VerifyActivity.class);
                intent.putExtra("email", email); // Assuming userEmail is the email used for verification
                startActivity(intent);
                finish();
                startActivity(intent);
            } else if (apiResponse.getSuccess() == -1) {
                // Handle other custom error responses from the server
                Toast.makeText(this, "Not a valid email or name", Toast.LENGTH_SHORT).show();
            } else {
                // Unknown response, handle as needed
                Toast.makeText(this, "Unknown response. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignActivity.this,StartActivity.class));
        finish();
    }
}