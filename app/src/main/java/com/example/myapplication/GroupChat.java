package com.example.myapplication;

import android.content.Intent;
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


public class GroupChat extends Fragment implements ChatAdapter.OnUserClickListener{
    Socket socket;
    ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);
        socket = SocketSingleton.getSocketInstance(requireContext());
        recyclerView = view.findViewById(R.id.group_chat_recycler);
        initRecyclerView();

        socket.on("group-chat-list", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handleChatList(args[0], "GroupChatList");
            }
        });
        return view;
    }
    private void handleChatList(Object data, String jsonArrayKey) {
        if (data instanceof JSONObject) {
            JSONObject userData = (JSONObject) data;
            Log.d("Socket Data", "Received data: " + data.toString());

            // Update the RecyclerView based on the received data
            try {
                JSONArray userArray = userData.getJSONArray(jsonArrayKey);
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
        if (socket.connected()) {
            socket.disconnect();
        }
    }
}