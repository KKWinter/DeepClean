package com.ddong.qingjie.sys;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;

import com.ddong.qingjie.utils.YLog;
import com.ddong.qingjie.sys.CleanMemory.Listener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

public class CleanMemory {

    public interface Listener {
        public void OnCleanListener(String pid, String pkg, String name, String ver);
        public void OnCleanCompleteListener(String cleanMem);
    }

    private CleanMemory.Listener listener = null;
    private Activity activity = null;

    public CleanMemory(CleanMemory.Listener _listener, Activity _activity) {
        listener = _listener;
        activity = _activity;
    }

    public void startClean() {
        new CleanThread(listener, activity).start();
    }

    private class CleanThread extends Thread {

        private CleanMemory.Listener listener = null;
        private Activity activity = null;

        public CleanThread(CleanMemory.Listener _listener, Activity _activity) {
            listener = _listener;
            activity = _activity;
        }

        private long getCurrectMemInfo() {
            long memory = 0;
            try {
                ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                MemoryInfo mi = new MemoryInfo();
                am.getMemoryInfo(mi);

                String filePath = "/proc/meminfo";
                String tmpStr;
                String[] arrayOfString;

                FileReader localFileReader = new FileReader(filePath);
                BufferedReader localBufferedReader = new BufferedReader(
                        localFileReader, 8192);
                tmpStr = localBufferedReader.readLine();
                arrayOfString = tmpStr.split("\\s+");
                memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
                localBufferedReader.close();

                //memory获取内存空间总大小
                //mi.availMem获取内存空间可用大小
                //获取内存空间已经使用的大小了
                memory = memory - mi.availMem;
            } catch (Exception e) {
                YLog.d(e.getMessage());
            }
            return memory;
        }

        @Override
        public void run() {
            ActivityManager activityManger = (ActivityManager) activity.getSystemService(Activity.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
            if (list != null) {
                long start = getCurrectMemInfo();

                for (int i = 0; i < list.size(); i++) {
                    ActivityManager.RunningAppProcessInfo apinfo = list.get(i);

                    String[] pkgList = apinfo.pkgList;

                    if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                        for (int j = 0; j < pkgList.length; j++) {
                            try {
                                PackageInfo pkg = activity.getPackageManager().getPackageInfo(apinfo.pkgList[j], 0);
                                String appName = pkg.applicationInfo.loadLabel(activity.getPackageManager()).toString();
                                String versionName = pkg.versionName;
                                HashMap<String, Object> obj = new HashMap<>();
                                obj.put("listener", listener);
                                obj.put("pid", apinfo.pid);
                                obj.put("pn", appName);
                                obj.put("ver", versionName);
                                obj.put("pkg", apinfo.pkgList[j]);

                                //本次要清理的进程的信息
                                Message msg = new Message();
                                msg.what = 0;
                                msg.obj = obj;
                                handler.dispatchMessage(msg);
                                activityManger.killBackgroundProcesses(apinfo.pkgList[j]);
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } catch (NameNotFoundException e) {
                                YLog.d(e.getMessage());
                            } catch (Exception e) {
                                YLog.d(e.getMessage());
                            }
                        }
                    }
                }

                long end = getCurrectMemInfo();
                HashMap<String, Object> obj = new HashMap<>();
                obj.put("listener", listener);
                obj.put("mem", Formatter.formatFileSize(activity.getBaseContext(), start - end));

                //本次清理出来的内存大小
                Message msg = new Message();
                msg.what = 1;
                msg.obj = obj;
                handler.dispatchMessage(msg);
            }
        }
    }

    private static Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            handler.post(new InfoRunable(msg));
        }
    };
}

class InfoRunable implements Runnable {
    private Message msg = null;

    public InfoRunable(Message _msg) {
        msg = _msg;
    }

    @Override
    public void run() {

        @SuppressWarnings("unchecked")
        HashMap<String, Object> obj = (HashMap<String, Object>) msg.obj;
        Listener listener = (Listener) obj.get("listener");
        if (msg.what == 0) {
            listener.OnCleanListener(String.valueOf(obj.get("pid")), String.valueOf(obj.get("pkg")), String.valueOf(obj.get("pn")), String.valueOf(obj.get("ver")));
        } else if (msg.what == 1) {
            listener.OnCleanCompleteListener(String.valueOf(obj.get("mem")));
        }
    }
}