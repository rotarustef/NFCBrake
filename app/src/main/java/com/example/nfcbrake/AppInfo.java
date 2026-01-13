package com.example.nfcbrake;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String name;
    public Drawable appIcon;
    public String printTime;
    public int progressBar;

    public AppInfo(String name, Drawable appIcon, String printTime, int progressBar){
        this.name = name;
        this.appIcon = appIcon;
        this.printTime = printTime;
        this.progressBar = progressBar;
    }
}
