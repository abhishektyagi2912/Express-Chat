package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SearchFragment extends Fragment implements UserRecyclerViewAdapter.OnUserClickListener{

    Socket socket;
    SearchView searchView;
    UserRecyclerViewAdapter recyclerViewAdapter;

    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.SearchRecycler);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        socket = SocketSingleton.getSocketInstance(requireContext());
        initRecyclerView();

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // Hide the keyboard when focus is lost (search is canceled)
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                JSONObject data = new JSONObject();
                try {
                data.put("Name",query);
                }catch (JSONException e){
                e.printStackTrace();
                }
                socket.emit("search-user", data);
                call();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                JSONObject data = new JSONObject();
                try {
                    data.put("Name",newText);
                    data.put("Type","Search");
                }catch (JSONException e){
                    e.printStackTrace();
                }
                socket.emit("search-user", data);
                call();
                return true;
            }
        });

        // Add the SearchView click listener
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the focus and show the search icon
                searchView.setIconified(false);
                searchView.setFocusable(true);
                searchView.clearFocus();
            }
        });

        return view;
    }
    public void call(){
        socket.on("searched-user", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args[0] instanceof JSONObject) {
                    JSONObject userData = (JSONObject) args[0];
                    Log.d("Socket Data", "Received data: " + args[0].toString());
                    // Now you have the user data as a JSONObject
                    // Handle the JSONObject according to your requirements

                    // For example, you might want to extract user data and update the adapter
                    try {
                        JSONArray userArray = userData.getJSONArray("Users"); // Replace "users" with the actual key
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerViewAdapter.updateData(userArray);
//                                Log.d("Adapter", "run: Send array to recycler adapter");
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        openKeyboard();
    }

    private void openKeyboard() {
        searchView.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT);
    }

    private void initRecyclerView() {
        recyclerViewAdapter = new UserRecyclerViewAdapter(requireContext(),this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onUserClick(String partnerId,String name) {
        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
        chatIntent.putExtra("PARTNER_ID", partnerId);
        chatIntent.putExtra("NAME", name);
        startActivity(chatIntent);
    }
}