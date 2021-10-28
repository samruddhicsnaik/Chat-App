package com.example.letschat;

public class People
{
    private String uid, name, about, image;
    private boolean isSelected = false;

    public People()
    {

    }
    public People(String uid, String name, String about, String image, boolean isSelected)
    {
        this.uid = uid;
        this.name = name;
        this.about = about;
        this.image = image;
        this.isSelected = isSelected;
    }
    public String getUid()
    {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAbout() {
        return about;
    }
    public void setAbout(String about) {
        this.about = about;
    }
    public String getImage() {
        return image;
    }
    public boolean isSelected() {
        return isSelected;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public void setImage(String image) {
        this.image = image;
    }
}
