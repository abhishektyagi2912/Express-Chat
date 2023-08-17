package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.socket.client.Socket;

public class SelfChat extends Fragment implements ChatAdapter.OnUserClickListener{
    Socket socket;
    ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private boolean isCachedDataLoaded = false; // Add this variable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_self_chat, container, false);
        socket = SocketSingleton.getSocketInstance(requireContext());
        recyclerView = view.findViewById(R.id.self_chat_recycler);
        initRecyclerView();

//        Array array = [{"_id":"64da7e950a0399b6041202ac","LastChat":"2023-08-14T19:48:55.911Z","Partner":"Abhishek","Unread":0}]
        String userId = UserData.userId;
        String LastChat =  getCurrentTime();
        String userName = "Self";
        String userEmail = UserData.email;
//        int Unread = 0;

        JsonObject inputObject = new JsonObject();
        inputObject.addProperty("LastChat", LastChat);
        inputObject.addProperty("Partner", userName);
        inputObject.addProperty("Unread", 0);
        inputObject.addProperty("_id", userId);

        // Convert "LastChat" to ISO 8601 format
        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String lastChatIsoFormat = "";
        try {
            Date date = inputFormat.parse(inputObject.get("LastChat").getAsString());
            lastChatIsoFormat = outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        inputObject.addProperty("LastChat", lastChatIsoFormat);

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(inputObject);

        try {
            JSONArray jsonArrayOrgJson = new JSONArray(jsonArray.toString());
            handleChatList(jsonArrayOrgJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
//        Log.d("Self chat", "onCreateView: "+jsonArray);
        return view;
    }

    private void handleChatList(JSONArray jsonArray) {
            saveDataToCache(jsonArray);
            chatAdapter.updateData(jsonArray);
    }
    private void initRecyclerView() {
        chatAdapter = new ChatAdapter(requireContext(),this);

        // Load cached data if available
        if (!isCachedDataLoaded) {
            JSONArray cachedUserArray = loadDataFromCache();
            if (cachedUserArray != null) {
                chatAdapter.updateData(cachedUserArray);
                isCachedDataLoaded = true; // Mark cached data as loaded
            }
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(chatAdapter);
    }
    @Override
    public void onUserClick(String partnerId, String name) {
        Intent chatIntent = new Intent(getActivity(), MainChatActivity.class);
        chatIntent.putExtra("PARTNER_ID", partnerId);
        chatIntent.putExtra("NAME", name);
        chatIntent.putExtra("Type","Self");
        startActivity(chatIntent);
    }

    private void saveDataToCache(JSONArray userArray) {
        // Save the fetched data to SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("SelfData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selfArray", userArray.toString());
        editor.apply();
    }

    private JSONArray loadDataFromCache() {
        // Load the cached data from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("SelfData", Context.MODE_PRIVATE);
        String cachedData = sharedPreferences.getString("selfArray", null);
        if (cachedData != null) {
            try {
                return new JSONArray(cachedData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!socket.connected()) {
            socket.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!socket.connected()) {
            socket.connect();
        }
    }
}