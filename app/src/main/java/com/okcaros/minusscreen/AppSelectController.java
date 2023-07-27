package com.okcaros.minusscreen;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppSelectController extends ConstraintLayout {
    private RecyclerView appListRcv;
    private AppAdapter appAdapter;
    private WindowManager mWindowManager;

    public AppSelectController(@NonNull Context context, int position) {
        super(context);
        init();
        initEvent();
    }

    public void init() {
        inflate(getContext(), R.layout.app_select, this);

        appListRcv = findViewById(R.id.app_list_rcv);
        appListRcv.setLayoutManager(new LinearLayoutManager(getContext()));

        // get installed app list
        List<ApplicationInfo> installedApps = getInstalledApps();

        // 创建适配器并设置给 RecyclerView
        appAdapter = new AppAdapter(installedApps);
        appListRcv.setAdapter(appAdapter);
    }

    public void initEvent() {
        View view = this;
        findViewById(R.id.app_select_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                mWindowManager.removeView(view);
            }
        });
    }

    private List<ApplicationInfo> getInstalledApps() {
        PackageManager packageManager = getContext().getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(0);
        List<ApplicationInfo> filteredApps = new ArrayList<>();

        for (ApplicationInfo appInfo : installedApps) {
            // 过滤系统应用
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                filteredApps.add(appInfo);
            }
        }

        return filteredApps;
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

        private List<ApplicationInfo> appList;

        public AppAdapter(List<ApplicationInfo> appList) {
            this.appList = appList;
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_item, parent, false);
            return new AppViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            ApplicationInfo appInfo = appList.get(position);

            // set app icon
            Drawable appIcon = appInfo.loadIcon(getContext().getPackageManager());
            holder.appIcon.setImageDrawable(appIcon);

            // set app name
            String appName = appInfo.loadLabel(getContext().getPackageManager()).toString();
            holder.appName.setText(appName);
        }

        @Override
        public int getItemCount() {
            return appList.size();
        }

        public class AppViewHolder extends RecyclerView.ViewHolder {
            ImageView appIcon;
            TextView appName;

            public AppViewHolder(@NonNull View itemView) {
                super(itemView);
                appIcon = itemView.findViewById(R.id.app_icon);
                appName = itemView.findViewById(R.id.app_name);
            }
        }
    }
}
