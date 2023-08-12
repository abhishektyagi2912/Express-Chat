package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import io.socket.client.Socket;

public class HomeFragment extends Fragment {

    Socket socket;
    ChatAdapter chatAdapter;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    FragmentAdapter fragmentAdapter;
    private final String[] titles = new String[]{"Personal", "Group", "Self"};
    private int bottomNavHeight; // To store the height of the BottomNavigationView

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
}