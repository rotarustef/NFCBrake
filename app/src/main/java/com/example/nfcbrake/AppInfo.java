package com.example.nfcbrake;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String name;
    public Drawable appIcon;
    public long timeMillis;
    public String printTime;
    public int progressBar;

    public AppInfo(String name, Drawable appIcon, long timeMillis){
        this.name = name;
        this.appIcon = appIcon;
        this.timeMillis = timeMillis;
        this.printTime = millisTimeBeautifier(timeMillis);
        this.progressBar = getProgress(this.timeMillis);
    }

    public long getTimeMillis(){
        return this.timeMillis;
    }
    public static String millisTimeBeautifier(long time){
        long timeMinutes = time / 1000 / 60;

        if (timeMinutes == 0)
            return time / 1000  + "sec";

        if (timeMinutes <= 60 * 24){
            if(timeMinutes < 60)
                return timeMinutes + "min";
            else {
                long hour =  timeMinutes / 60;
                long  min =  timeMinutes  % 60;
                return hour + "h " + min + "min";
            }
        } else {
            return "1 day+";
        }
    }

    // progressBar (app time) / 24h
    public static int getProgress(long timeMillis){
        double appMinutes = (double) timeMillis / 1000 / 60;
        return (int)(appMinutes / (60 * 24) * 100);
    }
}
