package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.GroupChat;
import com.example.myapplication.PersonalChat;
import com.example.myapplication.SelfChat;

public class FragmentAdapter extends FragmentStateAdapter {

    private final String[] titles = new String[]{"Personal-Chat", "Group-Chat", "Self-Chat"};

    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        switch (position){
            case 0:
                return new PersonalChat();
            case 1:
                return new GroupChat();
            case 2:
                return new SelfChat();
        }
        return new PersonalChat();
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
