package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class HomeFragment extends Fragment {

    Socket socket;
    private TextView showAllTextView, personalChatTextView, groupChatTextView;
    private RecyclerView recyclerView;
    ChatAdapter chatAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        showAllTextView = view.findViewById(R.id.SHOW_ALL);
        personalChatTextView = view.findViewById(R.id.Personal_Chat);
        groupChatTextView = view.findViewById(R.id.Group_Chat);
        recyclerView = view.findViewById(R.id.mainActivity_RecyclerView);

        // add socket
        socket = SocketSingleton.getSocketInstance(requireContext());

        initRecyclerView();
        // play with colors
//        showAllTextView.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        showAllTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.text_view_selector));
        showAllTextView.setTextColor(Color.BLACK);

        personalChatTextView.setTextColor(Color.WHITE);

        groupChatTextView.setTextColor(Color.WHITE);

        // Home Page fragment
        showAllTextView.setOnClickListener(v -> {
            updateRecyclerViewContent("Show All");
            showAllTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.text_view_selector));
            showAllTextView.setTextColor(Color.BLACK);
            // Reset the other TextViews' background tint and text color
            personalChatTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.black_border));

            personalChatTextView.setTextColor(Color.WHITE);
            groupChatTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.black_border));

            groupChatTextView.setTextColor(Color.WHITE);

        });
        personalChatTextView.setOnClickListener(v -> {
            updateRecyclerViewContent("Personal Chat");
            // Update the clicked TextView's background tint and text color
            personalChatTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.text_view_selector));
            personalChatTextView.setTextColor(Color.BLACK);
            // Reset the other TextViews' background tint and text color
            showAllTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.black_border));
//            showAllTextView.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            showAllTextView.setTextColor(Color.WHITE);
            groupChatTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.black_border));
//            groupChatTextView.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            groupChatTextView.setTextColor(Color.WHITE);

        });
        groupChatTextView.setOnClickListener(v -> {
            updateRecyclerViewContent("Group Chat");
            // Update the clicked TextView's background tint and text color
            groupChatTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.text_view_selector));
            groupChatTextView.setTextColor(Color.BLACK);
            // Reset the other TextViews' background tint and text color
            showAllTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.black_border));
//            showAllTextView.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));

            showAllTextView.setTextColor(Color.WHITE);
            personalChatTextView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.black_border));
//            personalChatTextView.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            personalChatTextView.setTextColor(Color.WHITE);
        });

        // Default content to show when the app starts
        updateRecyclerViewContent("Show All");


        return view;
    }

    private void updateRecyclerViewContent(String category) {
        if(Objects.equals(category, "Show All")){
            socket.on("personal-chat-list", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args[0] instanceof JSONObject) {
                        JSONObject userData = (JSONObject) args[0];
                        Log.d("Socket Data", "Received data: " + args[0].toString());
                        // Now you have the user data as a JSONObject
                        // Handle the JSONObject according to your requirements

                        // For example, you might want to extract user data and update the adapter
                        try {
                            JSONArray userArray = userData.getJSONArray("PersonalChatList");
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
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
            });
        }

    }
    private void initRecyclerView() {
        chatAdapter = new ChatAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(chatAdapter);
    }
}