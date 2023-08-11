package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivityAdapter extends RecyclerView.Adapter<ChatActivityAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;

    private static final int LAYOUT_SENT = 1;
    private static final int LAYOUT_RECEIVED = 2;

    public ChatActivityAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutResource = viewType == LAYOUT_SENT
                ? R.layout.chat_msg_system
                : R.layout.chat_recieve_system;

        View view = LayoutInflater.from(context).inflate(layoutResource, parent, false);
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bindMessage(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isSentByUser() ? LAYOUT_SENT : LAYOUT_RECEIVED;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageContent;
        private TextView timestamp;


        MessageViewHolder(View itemView, int viewType) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.textMessage);
            timestamp = itemView.findViewById(R.id.textDateTime);

            if (viewType == LAYOUT_SENT) {
                messageContent.setBackgroundResource(R.drawable.background_sent_msgs);
            } else {
                messageContent.setBackgroundResource(R.drawable.background_recieve_msgs);
            }
        }

        void bindMessage(Message message) {
            messageContent.setText(message.getContent());
            timestamp.setText(message.getTimestamp());
        }
    }
}



