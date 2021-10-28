package com.example.letschat;

public class Group
{
    private String gid, name, date, time, admin, message;
    public Group() {

    }
    public Group(String gid, String name, String date, String time, String admin, String message) {
        this.gid = gid;
        this.name = name;
        this.date = date;
        this.time = time;
        this.admin = admin;
        this.message = message;
    }
    public String getGid() {
        return gid;
    }
    public void setGid(String gid) {
        this.gid = gid;
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
    public String getAdmin() {
        return admin;
    }
    public void setAdmin(String admin) {
        this.admin = admin;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
