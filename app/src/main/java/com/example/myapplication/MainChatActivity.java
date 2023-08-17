package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.myapplication.databinding.ActivityMainChatBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainChatActivity extends AppCompatActivity {

    ActivityMainChatBinding binding;
    Socket socket;
    ChatActivityAdapter adapter;

    List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        // Initialize the binding object
        binding = ActivityMainChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatActivityRecycler.setLayoutManager(layoutManager);

        // initialise socket
        socket = SocketSingleton.getSocketInstance(this);

        binding.chatActivityBack.setOnClickListener(v -> MainChatActivity.super.onBackPressed());

        // get data from activities
        String id = getIntent().getStringExtra("PARTNER_ID");  // chat id if it come from group and personal chat
        Log.d("chat id", id);
        String name = getIntent().getStringExtra("NAME");
        binding.chatUsername.setText(name);
        String type = getIntent().getStringExtra("Type");

        // set recycler view
        adapter = new ChatActivityAdapter(this, messageList,id);
        binding.chatActivityRecycler.setAdapter(adapter);
        if(Objects.equals(type, "Search")){
            searchUser(id);
        }else if(Objects.equals(type, "Personal")){
            RequestPersonalMessages(id);
        } else if (Objects.equals(type, "Group")) {
            RequestGroupMessages(id);
        } else if (Objects.equals(type, "Self")) {
            RequestSelfMessages(UserData.userId);
        }

    }
    // this is all about search fragment to the chatActivity
    private void searchUser(String id) {
        // Send request to search user
        JSONObject user = new JSONObject();
        try {
            user.put("Receiver", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("check-chat", user);

        socket.on("create-chat-result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject responseJson = (JSONObject) args[0]; // Assuming the JSON is the first argument
                    Log.d("Response", "call: "+ responseJson);
                    int exists = responseJson.getInt("Exists");
                    String partnerName = responseJson.getString("Partner");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (exists == 1) {
                                // Fetch chat messages using the chat ID
                                JSONObject responseJson = (JSONObject) args[0];
                                String ChatId = null;
                                try {
                                    ChatId = responseJson.getString("ChatId");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                RequestPersonalMessages(ChatId);
//                                String ChatId = responseJson.getString("ChatId");
                            } else if(exists == 0){
                                // Show a button to start the chat
                                binding.startChatButton.setVisibility(View.VISIBLE);
                                binding.startChatText.setVisibility(View.VISIBLE);
                                binding.chatEditText.setVisibility(View.INVISIBLE);
                                binding.chatSend.setVisibility(View.INVISIBLE);

                                binding.startChatButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        binding.startChatButton.setVisibility(View.INVISIBLE);
                                        binding.startChatText.setVisibility(View.INVISIBLE);
                                        binding.chatEditText.setVisibility(View.VISIBLE);
                                        binding.chatSend.setVisibility(View.VISIBLE);
                                        // Send request to create user
                                        JSONObject user = new JSONObject();
                                        try {
                                            user.put("Receiver", id);
                                            user.put("Partner", partnerName);
                                            user.put("UserName",UserData.name);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        socket.emit("create-personal-chat", user);

                                        // receive personal chat id
                                        socket.on("create-personal-chat-creator", new Emitter.Listener() {
                                            @Override
                                            public void call(Object... args) {
                                                if (args.length > 0) {

                                                    // Perform UI updates on the main thread
                                                    try {
                                                        JSONObject chatData  = (JSONObject) args[0];
                                                        Log.d("Socket Data", "chat activity" + args[0].toString());
                                                        String ChatId = chatData.getString("ChatId");
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                RequestPersonalMessages(ChatId);
                                                            }
                                                        });
                                                    } catch (JSONException e) {
                                                        throw new RuntimeException(e);
                                                    }

                                                }
                                            }
                                        });
                                    }
                                });
                            }

                            // Update UI with partner name
//                            binding.chatUsername.setText(partnerName);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // fetch personal messages
    private void RequestPersonalMessages(String ChatId){
        // Send request to fetch personal chat
        JSONObject fetch = new JSONObject();
        try {
            fetch.put("ChatId", ChatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("fetch-personal-chat", fetch);

        socket.on("personal-chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    JSONObject chatData  = (JSONObject) args[0];
                    Log.d("Socket Data", "chat activity" + args[0].toString());
                    // Perform UI updates on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handlePersonalChat(chatData);
                        }
                    });
                }
            }
        });
    }
    private void handlePersonalChat(JSONObject chatData){
        try {
            JSONArray messagesArray = chatData.getJSONArray("Messages");
            List<Message> newMessages = new ArrayList<>();

            if (messagesArray.length() == 0) {
                // No messages in the conversation, add a placeholder message if desired
                // For example, add a message that says "No messages yet"
                return;
            }

            for (int i = messagesArray.length() - 1; i >= 0; i--) {
                JSONObject messageObject = messagesArray.getJSONObject(i);
                String senderId = messageObject.getString("Sender");
                String content = messageObject.getString("Content");
                String timestamp = messageObject.getString("Timestamp");

                boolean isSentByUser = senderId.equals(UserData.userId);

                // Determine the layout resource based on the sender
                int layoutResource = isSentByUser ? R.layout.chat_msg_system : R.layout.chat_recieve_system;

                String time = timestamp.substring(11, 16);

                Message message = new Message(senderId, content, time, isSentByUser, layoutResource);

                // Only add the message if it's not already in the messageList
                if (!isMessageInList(message)) {
                    newMessages.add(message);
                }
            }

            if (newMessages.size() > 0) {
                // Add new messages to the list and notify adapter
                messageList.addAll(0, newMessages);
                adapter.notifyDataSetChanged();

                // Scroll to the bottom, which is now the latest message
                binding.chatActivityRecycler.smoothScrollToPosition(messageList.size() - 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void RequestGroupMessages(String ChatId) {
    }
    private void RequestSelfMessages(String userId) {
    }
    private boolean isMessageInList(Message message) {
        for (Message existingMessage : messageList) {
            if (existingMessage.getSenderId().equals(message.getSenderId())
                    && existingMessage.getContent().equals(message.getContent())
                    && existingMessage.getTimestamp().equals(message.getTimestamp())) {
                return true;
            }
        }
        return false;
    }
}