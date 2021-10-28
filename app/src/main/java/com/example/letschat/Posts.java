package com.example.letschat;

public class Posts
{
    public String name, date, time, post, postID, type, user;
    public Posts()
    {

    }
    public Posts(String name, String date, String time, String post, String postID, String type, String user) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.post = post;
        this.postID = postID;
        this.type = type;
        this.user = user;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public String getPost() {
        return post;
    }
    public void setPost(String post) {
        this.post = post;
    }
    public String getPostID() {
        return postID;
    }
    public void setPostID(String postID) {
        this.postID = postID;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
}
