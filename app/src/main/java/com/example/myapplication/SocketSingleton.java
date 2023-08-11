package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketSingleton {

    private static Socket socketInstance;

    private SocketSingleton() {
        // Private constructor to prevent instantiation
    }

    public static synchronized Socket getSocketInstance(Context context) {
        if (socketInstance == null) {
            // Get access tokens from SharedPreferences
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String accessToken = sharedPreferences.getString("accessToken", "");
            String refreshToken = sharedPreferences.getString("refreshToken", "");

            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.secure = true;
            options.query = "accessToken=" + accessToken + "&refreshToken=" + refreshToken + "&source=app";

            try {
                socketInstance = IO.socket("https://bharatchat.onrender.com", options);
                socketInstance.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return socketInstance;
    }
}

