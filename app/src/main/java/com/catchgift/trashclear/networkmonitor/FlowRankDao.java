package com.catchgift.trashclear.networkmonitor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;


import com.catchgift.trashclear.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Antony on 2016/1/10.
 */
public class FlowRankDao {

    private static ApplicationInfo applicationInfo;

    public static List<AppInfo> getAllApps(Context context){
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        //获取所有的已经安装的应用
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : installedPackages) {
            String packgeName = packageInfo.packageName;
            AppInfo appInfo = new AppInfo();
            try {
                applicationInfo = pm.getApplicationInfo(packgeName, 0);
                //应用名称
                appInfo.name = applicationInfo.loadLabel(pm).toString();
                //应用的图标
                appInfo.icon = applicationInfo.loadIcon(pm);

                //获取应用的uid信息
                int uid = applicationInfo.uid;
                //通过uid获取流量信息
                long UidRxBytes = TrafficStats.getUidRxBytes(uid); //总接收量
                long UidTxBytes = TrafficStats.getUidTxBytes(uid); //总发送量
                appInfo.size = UidRxBytes + UidTxBytes;

            }catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();

                //如果进程没有应用名称,就拿包名作为其应用名称
                appInfo.name = packgeName;
                //如果进程没有应用图标,拿当前应用的icon作为其默认图标
                appInfo.icon = context.getResources().getDrawable(R.drawable.ic_launcher);
            }

            if (appInfo.size != 0){
                appList.add(appInfo);
            }
        }
        return appList;
    }


}
