package com.example.letschat;

import android.app.Application;
import android.os.SystemClock;

public class ChatApp extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        SystemClock.sleep(1000);
    }
}
