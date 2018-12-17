package com.ddong.qingjie.sys;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ddong.qingjie.appfragment.AppDataInfo;
import com.ddong.qingjie.sys.InstAppInfo.Listenenr;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;

public class InstAppInfo {

    /**
     * App监听接口
     */
    public interface Listenenr {
        public void getAppList(HashMap<String, ArrayList<AppDataInfo>> map);
    }

    public static Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                handler.post(new ListInfoRunable(msg));
            }
        }

        ;
    };

    public static void getInstAppinfo(Listenenr listenenr, Context context) {
        new thread(listenenr, handler, context).start();
    }
}

class thread extends Thread {
    private Listenenr listener = null;
    private Handler handler = null;
    private Context context;

    private PackageManager pm;
    private ArrayList<AppDataInfo> userList;
    private ArrayList<AppDataInfo> sysList;

    public thread(Listenenr _listenenr, Handler _handler, Context _context) {
        listener = _listenenr;
        handler = _handler;
        context = _context;
    }

    // 系统函数，字符串转换 long -String (kb)
    public String formateFileSize(long size) {
        return Formatter.formatFileSize(context, size);
    }

    public void run() {

        // 获得PackageManager对象
        pm = context.getPackageManager();

        // 用户APP
        userList = new ArrayList<>();

        // 系统APP
        sysList = new ArrayList<>();

        List<PackageInfo> packages = pm.getInstalledPackages(0);

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);

            // 包名
            String packageName = packageInfo.packageName;

            //通过反射获获取应用缓存大小
            try {
                Class<?> clazz = Class.forName("android.content.pm.PackageManager");
                Method method = clazz.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                method.invoke(pm, packageName, mStatsObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    final IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {

        /**
         * stats 数据都封装在其中
         * succeeded 表示回调成功
         */
        @Override
        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
            long cacheSize = stats.cacheSize;   //缓存大小
            long codeSize = stats.codeSize;     //应用大小
            long dataSize = stats.dataSize;     //数据大小

            long totalSize = cacheSize + codeSize + dataSize;

            String packageName = stats.packageName;
            AppDataInfo apps = new AppDataInfo();

            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);


                apps.setAppDrawable(applicationInfo.loadIcon(pm));
                apps.setAppName(applicationInfo.loadLabel(pm).toString());
                apps.setPackageName(packageName);
                apps.setTotalsize(totalSize);

                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    userList.add(apps);
                    apps = null;
                } else {
                    sysList.add(apps);
                    apps = null;
                }

            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            if (succeeded) {
                HashMap<String, ArrayList<AppDataInfo>> map = new HashMap<String, ArrayList<AppDataInfo>>();
                map.put("user", userList);
                map.put("sys", sysList);

                HashMap<String, Object> obj = new HashMap<String, Object>();
                obj.put("data", map);
                obj.put("listener", listener);
                Message msg = new Message();
                msg.obj = obj;
                msg.what = 0;
                handler.dispatchMessage(msg);
            }
        }
    };
}

class ListInfoRunable implements Runnable {
    private Message msg = null;

    public ListInfoRunable(Message _msg) {
        msg = _msg;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        HashMap<String, Object> obj = (HashMap<String, Object>) msg.obj;
        HashMap<String, ArrayList<AppDataInfo>> map = (HashMap<String, ArrayList<AppDataInfo>>) obj.get("data");
        Listenenr listenenr = (Listenenr) obj.get("listener");
        listenenr.getAppList(map);
    }
}