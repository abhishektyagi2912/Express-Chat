package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class HomeFragment extends Fragment{

    Socket socket;
    ChatAdapter chatAdapter;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    FragmentAdapter fragmentAdapter;

//    FragmentA
    private final String[] titles = new String[]{"Personal", "Group", "Self"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // add socket
        socket = SocketSingleton.getSocketInstance(requireContext());

        viewPager2 = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.include);

        fragmentAdapter = new FragmentAdapter(getActivity());
        viewPager2.setAdapter(fragmentAdapter);

        new TabLayoutMediator(tabLayout,viewPager2,((tab, position) -> tab.setText(titles[position]))).attach();


        return view;
    }


//    private void initRecyclerView() {
//        chatAdapter = new ChatAdapter(requireContext(),this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setAdapter(chatAdapter);
//    }
//
//    @Override
//    public void onUserClick(String partnerId, String name) {
//        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
//        chatIntent.putExtra("PARTNER_ID", partnerId);
//        chatIntent.putExtra("NAME", name);
//        startActivity(chatIntent);
//    }

}