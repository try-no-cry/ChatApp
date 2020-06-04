package com.example.chatapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class FirstClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
