package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class HomeFragment extends Fragment {
    private TextView showAllTextView, personalChatTextView, groupChatTextView;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        showAllTextView = view.findViewById(R.id.SHOW_ALL);
        personalChatTextView = view.findViewById(R.id.Personal_Chat);
        groupChatTextView = view.findViewById(R.id.Group_Chat);
        recyclerView = view.findViewById(R.id.mainActivity_RecyclerView);

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

    }
}