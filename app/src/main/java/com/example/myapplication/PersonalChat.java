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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class PersonalChat extends Fragment implements ChatAdapter.OnUserClickListener{
    Socket socket;
    ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_personal_chat, container, false);
        socket = SocketSingleton.getSocketInstance(requireContext());
        recyclerView = view.findViewById(R.id.personal_chat_recycler);
        initRecyclerView();

        socket.on("personal-chat-list", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleChatList(args[0], "PersonalChatList");
            }
        });

        return view;
    }
    private void handleChatList(Object data, String jsonArrayKey) {
        if (data instanceof JSONObject) {
            JSONObject userData = (JSONObject) data;
//            Log.d("Socket Data", "Received data: " + data.toString());
            // Update the RecyclerView based on the received data
            try {
                JSONArray userArray = userData.getJSONArray(jsonArrayKey);
                saveDataToCache(userArray);
                // Ensure the RecyclerView update is done on the main UI thread
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.updateData(userArray);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void initRecyclerView() {
        chatAdapter = new ChatAdapter(requireContext(),this);

        // Load cached data if available
        JSONArray cachedUserArray = loadDataFromCache();
        if (cachedUserArray != null) {
            chatAdapter.updateData(cachedUserArray);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(chatAdapter);
    }
    @Override
    public void onUserClick(String partnerId, String name) {
        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
        chatIntent.putExtra("PARTNER_ID", partnerId);
        chatIntent.putExtra("NAME", name);
        startActivity(chatIntent);
    }

    private void saveDataToCache(JSONArray userArray) {
        // Save the fetched data to SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ChatData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userArray", userArray.toString());
        editor.apply();
    }

    private JSONArray loadDataFromCache() {
        // Load the cached data from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("ChatData", Context.MODE_PRIVATE);
        String cachedData = sharedPreferences.getString("userArray", null);
        if (cachedData != null) {
            try {
                return new JSONArray(cachedData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        socket.connect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socket.disconnect();
        socket.off("personal-chat-list");
    }
}