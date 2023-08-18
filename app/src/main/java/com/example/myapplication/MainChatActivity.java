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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

        // add best animation
        // Add a layout change listener to the RecyclerView
        binding.chatActivityRecycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    // Keyboard opened
                    binding.chatActivityRecycler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollToLastMessage();
                        }
                    }, 100); // Delay is added to ensure smooth scrolling
                }
            }
        });
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

        // chat is open it's mean chat is read
        JSONObject open = new JSONObject();
        try {
            open.put("ChatId", ChatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("read-personal-message", open);

        // personal msg receive
        socket.on("receive-personal-message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    // Check if the data is a JSON object
                    if (args[0] instanceof JSONObject) {
                        // Access the "accessToken" field and log its value
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String ChatId = data.getString("ChatId");
                            String Msg = data.getString("Content");
//                            Log.e("Socket.IO", "Msg" + Msg);
                            String time = getCurrentTime();

                            // Create a new Message object for the sender
                            Message message = new Message(UserData.userId, Msg, time, false, R.layout.chat_recieve_system);

                            // Update the UI on the main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Add the new message to the list and notify the adapter
                                    messageList.add(0,message);
                                    adapter.notifyItemInserted(messageList.size() - 1);

                                    // Scroll to the newly received message
                                    binding.chatActivityRecycler.scrollToPosition(messageList.size() - 1);
                                }
                            });
                        } catch (JSONException e) {
                            // Handle JSON parsing error if necessary
                            Log.e("Socket.IO", "JSON parsing error: " + e.getMessage());
                        }
                    } else {
                        // The data is not a JSON object
                        Log.e("Socket.IO", "Received data is not a JSON object");
                    }
                }
            }
        });

        // read msg ack
        socket.on("read-message-ack", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    // Check if the data is a JSON object
                    if (args[0] instanceof JSONObject) {
                        // Access the "accessToken" field and log its value
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String ChatId = data.getString("ChatId");
//                            Log.d("Socket","read-msg come");
                            Log.e("Socket.IO", "Chat Id" + ChatId);

                        } catch (JSONException e) {
                            // Handle JSON parsing error if necessary
                            Log.e("Socket.IO", "JSON parsing error: " + e.getMessage());
                        }
                    } else {
                        // The data is not a JSON object
                        Log.e("Socket.IO", "Received data is not a JSON object");
                    }
                }
            }
        });

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
//                    Log.d("Socket Data", "chat activity" + args[0].toString());
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

        // send msg with text
        binding.chatSend.setOnClickListener(v -> sendMessage(ChatId,UserData.userId));
    }

    // group chat requests
    private void RequestGroupMessages(String ChatId) {

        // chat is open it's mean chat is read
        JSONObject open = new JSONObject();
        try {
            open.put("ChatId", ChatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("read-group-message", open);
        // receive that msg is read or not

//        socket

        //fetch group chat
        JSONObject data = new JSONObject();
        try {
            data.put("ChatId", ChatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("fetch-group-chat", data);

        socket.on("group-chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    JSONObject chatData  = (JSONObject) args[0];
                    Log.d("Socket Data", "chat activity" + args[0].toString());
                    // Perform UI updates on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleGroupChat(chatData);
                        }
                    });
                }
            }
        });
    }

    // self msg request
    private void RequestSelfMessages(String userId) {
        // Send request to fetch personal chat
        JSONObject fetch = new JSONObject();
        try {
            fetch.put("", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("fetch-self-chat", fetch);

        socket.on("self-chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    JSONObject chatData  = (JSONObject) args[0];
//                    Log.d("Socket Data", "chat activity" + args[0].toString());
                    // Perform UI updates on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleSelfChat(chatData);
                        }
                    });
                }
            }
        });
        binding.chatSend.setOnClickListener(v->sendSelfMessage(userId));
    }
    // send self msg
    private void sendSelfMessage(String userId) {
        String messageText = binding.chatEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Assuming you have a sender ID, you can modify this accordingly
            JSONObject ans = new JSONObject();
            try {
                ans.put("Content",messageText);
            }catch (JSONException e){
                e.printStackTrace();
            }
            socket.emit("send-self-message",ans);

            // Clear the input field
            binding.chatEditText.getText().clear();

            String currentTime = getCurrentTime(); // Implement getCurrentTime() method to HH:mm format
            Message message = new Message(userId, messageText, currentTime, true, R.layout.chat_msg_system);

            // Add the new message to the beginning of the list and notify the adapter
            messageList.add(message);
            adapter.notifyItemInserted(messageList.size() - 1);

            // Reload chat history after sending a message
            JSONObject fetch = new JSONObject();
            try {
                fetch.put("","" );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("fetch-self-chat", fetch);
        }
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
                messageList.addAll(0,newMessages);
                adapter.notifyDataSetChanged();

                // Scroll to the bottom, which is now the latest message
                binding.chatActivityRecycler.smoothScrollToPosition(messageList.size() - 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleGroupChat(JSONObject chatData) {

    }

    private void handleSelfChat(JSONObject chatData){
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
                String content = messageObject.getString("Content");
                String timestamp = messageObject.getString("Timestamp");


                String time = timestamp.substring(11, 16);

                Message message = new Message(UserData.userId, content, timestamp, true, R.layout.chat_msg_system);

                // Only add the message if it's not already in the messageList
                if (!isMessageInList(message)) {
                    newMessages.add(message);
                }
            }

            if (newMessages.size() > 0) {
                // Add new messages to the list and notify adapter
                messageList.addAll(0,newMessages);
                adapter.notifyDataSetChanged();

                // Scroll to the bottom, which is now the latest message
                binding.chatActivityRecycler.smoothScrollToPosition(messageList.size() - 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    // send messages
    private void sendMessage(String id, String userId){
        String messageText = binding.chatEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Assuming you have a sender ID, you can modify this accordingly
            JSONObject ans = new JSONObject();
            try {
                ans.put("ChatId", id);
                ans.put("Content",messageText);
            }catch (JSONException e){
                e.printStackTrace();
            }
            socket.emit("send-personal-message",ans);

            // Clear the input field
            binding.chatEditText.getText().clear();

            String currentTime = getCurrentTime(); // Implement getCurrentTime() method to HH:mm format
            Message message = new Message(UserData.userId, messageText, currentTime, true, R.layout.chat_msg_system);

            // Add the new message to the beginning of the list and notify the adapter
            messageList.add(message);
            adapter.notifyItemInserted(messageList.size() - 1);

            // Reload chat history after sending a message
            JSONObject fetch = new JSONObject();
            try {
                fetch.put("ChatId",id );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("fetch-personal-chat", fetch);
        }
    }
    // check msg is already insert ot not
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
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
    private void scrollToLastMessage() {
        if (messageList.size() > 0) {
            binding.chatActivityRecycler.smoothScrollToPosition(messageList.size() - 1);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!socket.connected()) {
            socket.connect();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (!socket.connected()) {
            socket.connect();
        }
    }
}