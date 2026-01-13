package com.example.nfcbrake;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<AppInfo> apps;

        try {
            apps = getAppInfo();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        AppAdapter adapter = new AppAdapter(apps);
        recyclerView.setAdapter(adapter);

    }

    public void requestPermission(Context context){
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        context.startActivity(intent);
    }
    public boolean isUsageAccessGranted() {
        UsageStatsManager check = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);

        List<UsageStats> checkList = check.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - 1000 * 3600 * 24, System.currentTimeMillis());
        return !checkList.isEmpty();
    }

    public String millisTimeBeautifier(long time){
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

    public List<AppInfo> getAppInfo() throws PackageManager.NameNotFoundException {

        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> appTime = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - (1000 * 3600 * 24), System.currentTimeMillis());

        appTime.sort(Comparator.comparingLong(UsageStats::getTotalTimeInForeground).reversed());
        PackageManager pm = getPackageManager();

        List<AppInfo> appList = new ArrayList<>();

        for(UsageStats stats: appTime){

            long filterSeconds = stats.getTotalTimeInForeground() / 1000 ;
            if (filterSeconds > 0) {

                // get the name of the package
                ApplicationInfo ai = pm.getApplicationInfo(stats.getPackageName(),0);
                String name = pm.getApplicationLabel(ai).toString();

                Drawable appIcon = getPackageManager().getApplicationIcon(stats.getPackageName());

                String stringTime = millisTimeBeautifier(stats.getTotalTimeInForeground());

                // progressBar (app time) / 24h
                double appMinutes = (double) stats.getTotalTimeInForeground() / 1000 / 60;
                int timeBar = (int)(appMinutes / (60 * 24) * 100);

                appList.add(new AppInfo(name, appIcon, stringTime, timeBar));
            }

        }

        return appList;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!isUsageAccessGranted()) {
            requestPermission(this);
            Toast.makeText(this, "Grant the necessary permission", Toast.LENGTH_LONG).show();
        }
    }
}