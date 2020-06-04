package com.example.chatapp;

public class    Contact {


    private  String name,status,uid;



    private String image;

    public Contact() {

    }

    public Contact(String name, String status, String uid,String image) {
        this.name = name;
        this.status = status;
        this.uid = uid;
        this.image=image;
    }

    public String getProfileImage() {
        return image;
    }

    public void setProfileImage(String image) {
        this.image = image;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
