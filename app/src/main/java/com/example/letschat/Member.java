package com.example.letschat;

public class Member
{
    private String user;
    private boolean isSelected = false;

    public Member()
    {

    }
    public Member(String user, boolean isSelected) {
        this.user = user;
        this.isSelected = isSelected;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public boolean isSelected() {
        return isSelected;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
