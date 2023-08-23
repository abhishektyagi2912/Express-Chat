package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.R;
import java.io.IOException;
import android.webkit.CookieManager;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

public class MainActivity extends AppCompatActivity implements SocketConnectionListener {
    private Socket socket;
    private SharedPreferences sharedPreferences;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                replaceFragment(new HomeFragment());
            }else if (item.getItemId() == R.id.menu_search) {
                replaceFragment(new SearchFragment());
            }else if (item.getItemId() == R.id.menu_calls) {
                replaceFragment(new CallFragment());
            }else if (item.getItemId() == R.id.menu_profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });


        // Access tokens
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String value1 = sharedPreferences.getString("accessToken", "");
        String value2 = sharedPreferences.getString("refreshToken", "");

        // Connect to server using ExecutorService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> connectToServer(value1, value2));
        executor.shutdown();
    }

    // most important to connect the servers
    private void connectToServer(String value1, String value2) {
        try {
            String encodedValue1 = URLEncoder.encode(value1, "UTF-8");
            String encodedValue2 = URLEncoder.encode(value2, "UTF-8");

            // Save the cookies to CookieManager
            CookieManager.getInstance().setCookie("https://expresschat-v6mg.onrender.com", "accessToken=" + encodedValue1);
            CookieManager.getInstance().setCookie("https://expresschat-v6mg.onrender.com", "refreshToken=" + encodedValue2);

            socket = SocketSingleton.getSocketInstance(this);

            // access tokens saved if available
            socket.on("access-token", args -> {
                // Check if there is any data received
                if (args.length > 0) {
                    // Check if the data is a JSON object
                    if (args[0] instanceof JSONObject) {
                        // Access the "accessToken" field and log its value
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String newAccessToken = data.getString("accessToken");
                            Log.e("Socket.IO", "Received access token: " + newAccessToken);

//                                 Update the access token in SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("accessToken", newAccessToken);
                            editor.apply();
                        } catch (JSONException e) {
                            // Handle JSON parsing error if necessary
                            Log.e("Socket.IO", "JSON parsing error: " + e.getMessage());
                        }
                    } else {
                        // The data is not a JSON object
                        Log.e("Socket.IO", "Received data is not a JSON object");
                    }
                }
            });

            // here socket connect request is connect
            runOnUiThread(() -> onConnected(socket));
        } catch (IOException e) {
            runOnUiThread(() -> onConnectionError(e));
        }
    }

    @Override
    public void onConnected(Socket socket) {
        // Connection successful, do something with the socket
        this.socket = socket;
        Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show();
        // Use the 'socket' here or perform any other actions after successful connection

        //helper function to fetch user details
        JSONObject user = new JSONObject();
        try {
            user.put("","");
        }catch (JSONException e){
            e.printStackTrace();
        }
        socket.emit("fetch-user-details",user);


        // user details fetch
        socket.on("user-details", args -> {
            if (args.length > 0) {
                // Check if the data is a JSON object
                if (args[0] instanceof JSONObject) {
                    // Access the "accessToken" field and log its value
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String email = data.getString("Email");
                        String name = data.getString("Name");
                        String userId = data.getString("Id");
                        UserData.userId = userId;
                        UserData.email = email;
                        UserData.name = name;
//                        Log.d("Socket", email);
//                        Log.d("Socket",name);
//                        Log.d("Socket",userId);

                    } catch (JSONException e) {
                        // Handle JSON parsing error if necessary
                        Log.e("Socket.IO", "JSON parsing error: " + e.getMessage());
                    }
                } else {
                    // The data is not a JSON object
                    Log.e("Socket.IO", "Received data is not a JSON object");
                }
            }
        });
    }

    @Override
    public void onConnectionError(Exception e) {
        // Connection failed, handle the error
        e.printStackTrace();
        Toast.makeText(this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
    }

    //fragments
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

}
