package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.ViewHolder> {
    private final Context context;
    private OnUserClickListener userClickListener;

    public interface OnUserClickListener {
        void onUserClick(String partnerId);
    }

    public UserRecyclerViewAdapter(Context context,OnUserClickListener userClickListener) {
        this.userClickListener = userClickListener;
        this.context = context;
    }
    private JSONArray userData = new JSONArray();

    public void updateData(JSONArray newData) {
        userData = newData;
        notifyDataSetChanged();
    }

    @Override
    public UserRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.users_item,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecyclerViewAdapter.ViewHolder holder, int position) {
        try {
            JSONObject userObject = userData.getJSONObject(position);
            String name = userObject.getString("Name");
            String email = userObject.getString("Email");
            String partnerId = userObject.getString("_id");
            holder.nameTextView.setText(name);
            holder.emailTextView.setText(email);
//            Log.d("Something", "onBindViewHolder: SET");
            holder.itemView.setOnClickListener(v -> {
                if (userClickListener != null) {
                    userClickListener.onUserClick(partnerId);
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView emailTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.SearchName);
            emailTextView = itemView.findViewById(R.id.SearchEmail);
//            Log.d("Set", "ViewHolder: Set name ");
        }
    }
}
