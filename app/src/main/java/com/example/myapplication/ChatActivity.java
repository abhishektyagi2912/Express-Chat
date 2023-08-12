package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity implements ChatActivityAdapter.OnItemClickListener{

    ImageView chat_activity_back,chat_send;
    TextView chat_username;
    EditText chat_edit_text;
    Socket socket;
    RecyclerView recyclerView;
    ChatActivityAdapter adapter;
    String id;
    List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_activity_back = findViewById(R.id.chat_activity_back);
        chat_username = findViewById(R.id.chat_username);
        chat_send = findViewById(R.id.chat_send);
        chat_edit_text = findViewById(R.id.chat_edit_text);

        // recycler view
        recyclerView = findViewById(R.id.chat_activity_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Add a layout change listener to the RecyclerView
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    // Keyboard opened
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollToLastMessage();
                        }
                    }, 100); // Delay is added to ensure smooth scrolling
                }
            }
        });


        // initialize socket
        socket = SocketSingleton.getSocketInstance(this);

        String userId = UserData.userId;
//        Log.d("socket", userId);
        id = getIntent().getStringExtra("PARTNER_ID");  // chat id this is
//        Log.d("socket", id);
        String name = getIntent().getStringExtra("NAME");


        adapter = new ChatActivityAdapter(this, messageList,id);
        recyclerView.setAdapter(adapter);

        chat_username.setText(name);
        chat_activity_back.setOnClickListener(v -> ChatActivity.super.onBackPressed());

        setUpSocketListeners(userId);

        // Send request to fetch personal chat
        JSONObject fetch = new JSONObject();
        try {
            fetch.put("ChatId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("fetch-personal-chat", fetch);

        // personal msg receive
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
                                    messageList.add(message);
                                    adapter.notifyItemInserted(messageList.size() - 1);

                                    // Scroll to the newly received message
                                    recyclerView.scrollToPosition(messageList.size() - 1);
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

        // send msg to read
        JSONObject acknowledgementData = new JSONObject();
        try {
            acknowledgementData.put("ChatId", id);
            Log.d("socket", String.valueOf(acknowledgementData));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("read-personal-message", acknowledgementData);

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

        // send chat
        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(id,UserData.userId);
            }
        });
    }
    private void sendMessage(String id, String userId) {
        String messageText = chat_edit_text.getText().toString().trim();
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
            String currentTime = getCurrentTime(); // Implement getCurrentTime() method to HH:mm format
            Message message = new Message(UserData.userId, messageText, currentTime, true, R.layout.chat_msg_system);

            // Add the message to the local list and notify the adapter
            messageList.add(message);
            adapter.notifyItemInserted(messageList.size() - 1);

            // Clear the input field
            chat_edit_text.getText().clear();

            // Reload chat history after sending a message
            JSONObject fetch = new JSONObject();
            try {
                fetch.put("ChatId", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("fetch-personal-chat", fetch);
        }
    }
    private void setUpSocketListeners(String userId) {
        // Listen for incoming messages
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
                            handleIncomingMessages(chatData, userId);
                        }
                    });
                }
            }
        });
    }



    private void handleIncomingMessages(JSONObject chatData, String userId) {
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
                recyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        JSONObject fetch = new JSONObject();
        try {
            fetch.put("ChatId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("fetch-personal-chat", fetch);
    }
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
    private void scrollToLastMessage() {
        if (messageList.size() > 0) {
            recyclerView.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    @Override
    public void onItemClick(String chatId, String userId) {
        sendAcknowledgement(chatId);
    }

    private void sendAcknowledgement(String chatId) {
        // Implement the code to send the acknowledgement using the chat ID
        Log.d("socket", chatId);
//        JSONObject acknowledgementData = new JSONObject();
//        try {
//            acknowledgementData.put("ChatId", chatId);
//            Log.d("socket", String.valueOf(acknowledgementData));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        socket.emit("read-personal-message", acknowledgementData);
        }
    }