package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;

public class StartActivity extends AppCompatActivity {

//    String
//    private App realmApp;
//    private static final String REALM_APP_ID = "bhart_chat-whcgp";
//    private static final String REALM_APP_NAME = "Bhart_Chat";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("accessToken", null);
        String refreshToken = sharedPreferences.getString("refreshToken", null);
        if (accessToken != null && refreshToken != null) {
            startMainActivity();
        }

    }
    public void already(View view){
        startActivity(new Intent(StartActivity.this,LoginActivity.class));
        finish();
    }

    public void getstart(View view){
        startActivity(new Intent(StartActivity.this,SignActivity.class));
        finish();
    }

    private void startMainActivity() {
         Intent intent = new Intent(this, MainActivity.class);
         startActivity(intent);
         finish();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}