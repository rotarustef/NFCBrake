package com.example.nfcbrake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private final List<AppInfo> appInfo;
    private final long totaUsageMillis;

    public AppAdapter(List<AppInfo> apps) {
        this.appInfo = apps;

        long sumUsage = 0L;
        for (AppInfo app : appInfo) {
            sumUsage += app.getTimeMillis();
        }

        this.totaUsageMillis = sumUsage;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final ImageView icon;

        private final TextView printTime;
        private final ProgressBar progress;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.appName);
            icon = itemView.findViewById(R.id.appIcon);
            printTime = itemView.findViewById(R.id.printTime);
            progress = itemView.findViewById(R.id.progressBarTotal);
        }

        public TextView getText() {
            return name;
        }

        public ImageView getIcon() {
            return icon;
        }

        public TextView getTime() {
            return printTime;
        }

        public ProgressBar getProgress() {
            return progress;
        }
    }

    public long getTotaUsageMillis() {
        return totaUsageMillis;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getText().setText(appInfo.get(position).getName());
        viewHolder.getIcon().setImageDrawable(appInfo.get(position).getAppIcon());
        viewHolder.getTime().setText(appInfo.get(position).getStringTime());
        viewHolder.getProgress().setProgress((int) (100.0 * appInfo.get(position).getTimeMillis() / totaUsageMillis));
    }

    @Override
    public int getItemCount() {
        return appInfo.size();
    }
}
