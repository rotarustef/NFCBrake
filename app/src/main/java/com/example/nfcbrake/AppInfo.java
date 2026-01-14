package com.example.nfcbrake;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private final String name;
    private final Drawable appIcon;
    private final long timeMillis;
    private final String printTime;

    public AppInfo(String name, Drawable appIcon, long timeMillis) {
        this.name = name;
        this.appIcon = appIcon;
        this.timeMillis = timeMillis;
        this.printTime = millisTimeBeautifier(timeMillis);
    }

    public String getName() {
        return this.name;
    }

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public long getTimeMillis() {
        return this.timeMillis;
    }

    public String getStringTime() {
        return this.printTime;
    }

    public static String millisTimeBeautifier(long time) {
        long timeMinutes = time / 1000 / 60;

        if (timeMinutes == 0)
            return time / 1000 + "sec";

        if (timeMinutes <= 60 * 24) {
            if (timeMinutes < 60)
                return timeMinutes + "min";
            else {
                long hour = timeMinutes / 60;
                long min = timeMinutes % 60;
                return hour + "h " + min + "min";
            }
        } else {
            return "1 day+";
        }
    }

    // progressBar (app time) / 24h
    public static int getProgress(long timeMillis) {
        double appMinutes = (double) timeMillis / 1000 / 60;
        return (int) (appMinutes / (60 * 24) * 100);
    }
}
