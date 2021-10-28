package com.example.letschat;

public class GroupMessages
{
    String date, time, message, user, type;
    boolean seen;
    public GroupMessages(String date, String time, String message, String user, String type, boolean seen) {
        this.date = date;
        this.time = time;
        this.message = message;
        this.user = user;
    }
    public GroupMessages() {

    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
