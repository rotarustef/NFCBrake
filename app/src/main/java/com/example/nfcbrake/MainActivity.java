package com.example.nfcbrake;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        updateMainLayout(recyclerView);
    }

    private void updateMainLayout(RecyclerView recyclerView) {

        List<AppInfo> apps;
        try {
            apps = getAppInfo();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        AppAdapter adapter = new AppAdapter(apps);
        recyclerView.setAdapter(adapter);

        TextView totalTime = findViewById(R.id.totalTime);
        ProgressBar totalProgress = findViewById(R.id.progressBarTotal);

        long totalTimeElapsed = 0;

        for ( AppInfo app: apps){
            totalTimeElapsed += app.getTimeMillis();
        }

        totalTime.setText(AppInfo.millisTimeBeautifier(totalTimeElapsed));
        totalProgress.setProgress(AppInfo.getProgress(totalTimeElapsed));
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



    public List<AppInfo> getAppInfo() throws PackageManager.NameNotFoundException {

        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);

        ZoneId timeZone = ZoneId.systemDefault();
        long startDay = LocalDate.now().atStartOfDay(timeZone).toInstant().toEpochMilli();

        Map<String, Long> foregroundApps = new HashMap<>();
        Map<String, Integer> activeApps = new HashMap<>();
        Map<String, Long> totalTime = new HashMap<>();

        long now = System.currentTimeMillis();
        UsageEvents events = usm.queryEvents(startDay, now);

        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()){
            events.getNextEvent(event);

            String pkgName = event.getPackageName();
            long eventTime = event.getTimeStamp();
            Integer foregroundCounter;

            switch(event.getEventType()){

                case UsageEvents.Event.ACTIVITY_RESUMED:
                    foregroundCounter = activeApps.get(pkgName);

                    if(foregroundCounter == null){
                        activeApps.put(pkgName, 1);
                        foregroundApps.put(pkgName, eventTime);
                    } else {
                        activeApps.put(pkgName, foregroundCounter + 1);
                    }
                    break;

                case UsageEvents.Event.ACTIVITY_PAUSED:
                    foregroundCounter = activeApps.get(pkgName);
                    if(foregroundCounter == null)
                        break;

                    if (foregroundCounter == 1) {
                    activeApps.remove(pkgName);

                    Long startTimeOfApp = foregroundApps.remove(pkgName);
                    if (startTimeOfApp != null) {
                        long delta = eventTime - startTimeOfApp;
                        totalTime.merge(pkgName, delta, Long::sum);
                    }
                    } else {
                        activeApps.put(pkgName, foregroundCounter - 1);
                    }
                    break;
            }
        }

        for (Map.Entry<String, Long> e : foregroundApps.entrySet()) {
            String pkgName = e.getKey();
            long startTime = e.getValue();

            long delta = now - startTime;
            totalTime.merge(pkgName, delta, Long::sum);
        }

        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = getPackageManager();

        for (Map.Entry<String, Long> e : totalTime.entrySet()) {
            String pkgName = e.getKey();

            // filter system apps that are not accessible by the user
            if (pm.getLaunchIntentForPackage(pkgName) == null)
                continue;

            ApplicationInfo ai = pm.getApplicationInfo(pkgName,0);
            String appName = pm.getApplicationLabel(ai).toString();

            Drawable appIcon = getPackageManager().getApplicationIcon(pkgName);
            long appTime = e.getValue();

            appList.add(new AppInfo(appName, appIcon, appTime));
        }

        appList.sort(Comparator.comparingLong(AppInfo::getTimeMillis).reversed());
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

    @Override
    protected void onResume(){
        super.onResume();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        updateMainLayout(recyclerView);
    }
}