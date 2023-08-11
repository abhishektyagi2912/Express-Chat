package com.example.myapplication;

import io.socket.client.Socket;

public interface SocketConnectionListener {

    void onConnected(Socket socket);

    void onConnectionError(Exception e);
}

