package com.example.myapplication;

public class Message {
    private String senderId;
    private String content;
    private String timestamp;
    private boolean isSentByUser;
    private int layoutResource;

    public Message(String senderId, String content, String timestamp, boolean isSentByUser, int layoutResource) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.isSentByUser = isSentByUser;
        this.layoutResource = layoutResource;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }

    public int getLayoutResource() {
        return layoutResource;
    }
}
