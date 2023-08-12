package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private OnUserClickListener userClickListener;

    public interface OnUserClickListener {
        void onUserClick(String partnerId, String name);
    }
    public ChatAdapter(Context context, OnUserClickListener userClickListener){
        this.userClickListener = userClickListener;
        this.context = context;
    }

    private JSONArray userData = new JSONArray();
    public void updateData(JSONArray newData) {
        userData = newData;
        notifyDataSetChanged();
    }
    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.chat_item,parent,false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ChatViewHolder holder, int position) {
        try {
            JSONObject userObject = userData.getJSONObject(position);

            String name;
            if (userObject.has("Partner")) {
                // Personal chat
                name = userObject.getString("Partner");
            } else if (userObject.has("Name")) {
                // Group chat
                name = userObject.getString("Name");
            } else {
                name = "Unknown"; // Handle the case where neither key is present
            }

//            String name = userObject.getString("Partner");
            String unread = userObject.getString("Unread");
            String partnerId = userObject.getString("_id");

            holder.partnerId = partnerId;
            holder.nameTextView.setText(name);
            holder.unread.setText(unread);
//            holder.nameTextView.setText(name);
            holder.itemView.setOnClickListener(v -> {
                if (userClickListener != null) {
                    userClickListener.onUserClick(holder.partnerId,name);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return userData.length();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView textWritten, time,unread;
        String partnerId;
        public ChatViewHolder(@NonNull View itemView) {

            super(itemView);
            nameTextView = itemView.findViewById(R.id.textName);
            textWritten = itemView.findViewById(R.id.textWritten);
            time  =itemView.findViewById(R.id.textTime);
            unread = itemView.findViewById(R.id.textCount);
        }
    }
}
