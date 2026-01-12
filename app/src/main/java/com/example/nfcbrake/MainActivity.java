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
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        requestPermission(this);
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - (1000 * 3600 * 24), System.currentTimeMillis());

        appList.sort(Comparator.comparingLong(UsageStats::getTotalTimeInForeground).reversed());

        for(UsageStats stats: appList){
            long filterSeconds = stats.getTotalTimeInForeground() / 1000 ;
            if (filterSeconds > 0)
                Log.d("Message", stats.getPackageName() + " - " + millisTimeBeautifier(stats.getTotalTimeInForeground()));
        }


        Drawable appIcon;

        try {
            appIcon = getPackageManager().getApplicationIcon(appList.get(0).getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        ImageView imgView = findViewById(R.id.imgView);
        TextView textBox = findViewById(R.id.textView2);

        imgView.setImageDrawable(appIcon);
        PackageManager pm = this.getPackageManager();



        try {
            ApplicationInfo ai = pm.getApplicationInfo(appList.get(0).getPackageName(),0);
            textBox.setText(pm.getApplicationLabel(ai).toString());
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        TextView timeElapsed = findViewById(R.id.textView4);
        timeElapsed.setText((millisTimeBeautifier(appList.get(0).getTotalTimeInForeground())));

        ProgressBar timeBar = findViewById(R.id.progressBar2);
        double appMinutes = (double) appList.get(0).getTotalTimeInForeground()/1000/60;
        timeBar.setProgress((int)(appMinutes/(60*2)*100));


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

    @Override
    protected void onStart() {
        super.onStart();

        if (!isUsageAccessGranted()) {
            requestPermission(this);
            Toast.makeText(this, "Grant the necessary permission", Toast.LENGTH_LONG).show();
        }
    }
}