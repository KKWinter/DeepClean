package com.catchgift.trashclear.appfragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.Message;
import com.catchgift.trashclear.appfragment.InitAppData.AppDataListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class InitAppData {

    public interface AppDataListener {
        void userAppDataListener(AppDataInfo appInfo);

        void sysAppDataListener(AppDataInfo appInfo);
    }

    public static Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            handler.post(new AppInfoRunable(msg));
        }
    };

    public static AppDataThread getAppInfo(AppDataListener listener, Context context) {
        AppDataThread appDataThread = new AppDataThread(listener, context, handler);
        appDataThread.start();
        return appDataThread;
    }

}

class AppDataThread extends Thread {
    AppDataListener listener = null;
    Context context = null;
    Handler handler = null;

    private PackageManager mPM;

    public Boolean closeThread;

    public AppDataThread(AppDataListener listener, Context context, Handler handler) {
        this.listener = listener;
        this.context = context;
        this.handler = handler;
        closeThread = false;
    }

    @Override
    public void run() {
        //包管理者对象
        mPM = context.getPackageManager();

        List<PackageInfo> installedPackages = mPM.getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {

            if (!closeThread){
                String packageName = packageInfo.packageName;

                //通过反射获获取应用、缓存和数据大小
                try {
                    Class<?> clazz = Class.forName("android.content.pm.PackageManager");
                    Method method = clazz.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                    method.invoke(mPM, packageName, mStatsObserver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        super.run();
    }

    final IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub(){
        private ApplicationInfo applicationInfo;

        //这个方法是在子线程中执行的
        @Override
        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
            if (closeThread){
                return;
            }

            //单位为byte
            long cacheSize = stats.cacheSize;   //缓存大小
            long codeSize = stats.codeSize;        //应用大小
            long dataSize = stats.dataSize;    //数据大小

            String packageName = stats.packageName;
            AppDataInfo appDataInfo = new AppDataInfo();

            try {
                applicationInfo = mPM.getApplicationInfo(packageName, 0);
                //名称
                appDataInfo.appName = applicationInfo.loadLabel(mPM).toString();
                //图标
                appDataInfo.appDrawable = applicationInfo.loadIcon(mPM);
                //包名
                appDataInfo.packageName = packageName;
                //大小
                appDataInfo.cacheSize = cacheSize;
                appDataInfo.codeSize = codeSize;
                appDataInfo.dataSize = dataSize;
                appDataInfo.totalsize = cacheSize + codeSize + dataSize;

            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            //判断是否系统应用
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appDataInfo.isSystem = false;
                //用户应用
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("appInfo", appDataInfo);
                userMap.put("listener", listener);
                Message msg = Message.obtain();
                msg.what = 0;
                msg.obj = userMap;
                handler.dispatchMessage(msg);

            } else {
                appDataInfo.isSystem = true;
                //系统应用
                HashMap<String, Object> sysMap = new HashMap<>();
                sysMap.put("appInfo", appDataInfo);
                sysMap.put("listener", listener);
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = sysMap;
                handler.dispatchMessage(msg);

            }
        }
    };
}

class AppInfoRunable implements Runnable {
    private Message msg = null;

    public AppInfoRunable(Message _msg) {
        msg = _msg;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        if (msg.what == 0) {
            HashMap<String, Object> hashMap = (HashMap<String, Object>) msg.obj;
            AppDataInfo appInfo = (AppDataInfo) hashMap.get("appInfo");
            AppDataListener listener = (AppDataListener) hashMap.get("listener");
            listener.userAppDataListener(appInfo);
        } else if (msg.what == 1) {
            HashMap<String, Object> hashMap = (HashMap<String, Object>) msg.obj;
            AppDataInfo appInfo = (AppDataInfo) hashMap.get("appInfo");
            AppDataListener listener = (AppDataListener) hashMap.get("listener");
            listener.sysAppDataListener(appInfo);
        }
    }
}
