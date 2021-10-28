package com.example.letschat;

public class Chats
{
    private String chatID, sender, receiver, user;
    private boolean seen;
    public Chats() {

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Chats(String chatID, String sender, String receiver, String user, boolean seen) {
        this.chatID = chatID;
        this.sender = sender;
        this.receiver = receiver;
        this.user = user;
        this.seen = seen;
    }

    public String getChatID() {
        return chatID;
    }
    public void setChatID(String chatID) {
        this.chatID = chatID;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getReceiver() {
        return receiver;
    }
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
}
