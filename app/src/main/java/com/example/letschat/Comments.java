package com.example.letschat;

public class Comments
{
    private String user, name, comment, date, time;
    public Comments()
    {

    }

    public Comments(String user, String name, String comment, String date, String time)
    {
        this.user = user;
        this.name = name;
        this.comment = comment;
        this.date = date;
        this.time = time;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
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
}
