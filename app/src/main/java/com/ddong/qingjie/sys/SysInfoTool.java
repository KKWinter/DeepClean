package com.ddong.qingjie.sys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.ddong.qingjie.utils.YLog;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.format.Formatter;

public class SysInfoTool {
    public interface Listener {
        public void ICPUInfoListener(String usage, String idle);

        public void IMemInfoListener(String used, String free, String total);

        public void IRootRomInfoListener(String used, String free, String total);

        public void IDataRomInfoListener(String used, String free, String total);
    }

    public void getSysinfo(Listener listener, Activity activity) {
        new TimerThread(listener, activity).start();
    }

    private static final int REFRESH = 0x000001;

    private class TimerThread extends Thread {

        private Listener listener = null;
        private Activity activity = null;

        public TimerThread(Listener _listener, Activity _activity) {
            listener = _listener;
            activity = _activity;
        }

        @Override
        public void run() {
            while (true) {
                Message msg = new Message();
                msg.what = REFRESH;
                HashMap<String, Object> obj = new HashMap<String, Object>();
                obj.put("listener", listener);
                obj.put("activity", activity);
                msg.obj = obj;
                handler.sendMessage(msg);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static Handler handler = new Handler() {
        private double usage = 0;
        private long total = 0;
        private long idle = 0;

        public void handleMessage(android.os.Message msg) {
            try {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> obj = (HashMap<String, Object>) msg.obj;
                Listener listener = (Listener) obj.get("listener");
                Activity activity = (Activity) obj.get("activity");

                getCupInfo(listener, activity);
                getMemInfo(listener, activity);
                getRootRomInfo(listener, activity);
                getDataRomInfo(listener, activity);

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception e) {
                YLog.d(e.getMessage());
            }
        }

        ;

        /**
         * 获取CPU信息
         * @param listener
         * @param activity
         * @throws IOException
         */
        private void getCupInfo(Listener listener, Activity activity) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();

            String[] toks = load.split(" ");

            long currTotal = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]);
            long currIdle = Long.parseLong(toks[5]);

            usage = (currTotal - total) * 100.0f / (currTotal - total + currIdle - idle);
            total = currTotal;
            idle = currIdle;

            java.text.DecimalFormat df = new java.text.DecimalFormat("#.#");
            String useage = df.format(usage);
            String idle = df.format(100 - usage);
            listener.ICPUInfoListener(useage, idle);
        }

        /**
         * 获取内存信息
         * @param listener
         * @param activity
         * @throws IOException
         */
        private void getMemInfo(Listener listener, Activity activity) throws IOException {
            ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            MemoryInfo mi = new MemoryInfo();
            am.getMemoryInfo(mi);

            String free = "";
            free = Formatter.formatFileSize(activity.getBaseContext(), mi.availMem);

            String filePath = "/proc/meminfo";
            String tmpStr;
            String[] arrayOfString;
            long initial_memory = 0;

            FileReader localFileReader = new FileReader(filePath);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            tmpStr = localBufferedReader.readLine();
            arrayOfString = tmpStr.split("\\s+");
            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
            localBufferedReader.close();

            String total = "";
            total = Formatter.formatFileSize(activity.getBaseContext(), initial_memory);
            String used = Formatter.formatFileSize(activity.getBaseContext(), initial_memory - mi.availMem);
            listener.IMemInfoListener(used, free, total);
        }

        /**
         * @param listener
         * @param activity
         */
        @SuppressWarnings("deprecation")
        private void getRootRomInfo(Listener listener, Activity activity) {
            File root = Environment.getRootDirectory();
            StatFs sf = new StatFs(root.getPath());
            long blockSize = sf.getBlockSize();
            long totalBlocks = sf.getBlockCount();
            long availableBlocks = sf.getAvailableBlocks();
            String total_rom_root = Formatter.formatFileSize(activity.getBaseContext(), totalBlocks * blockSize);
            String free_rom_root = Formatter.formatFileSize(activity.getBaseContext(), availableBlocks * blockSize);
            String used_rom_root = Formatter.formatFileSize(activity.getBaseContext(), totalBlocks * blockSize - availableBlocks * blockSize);
            listener.IRootRomInfoListener(used_rom_root, free_rom_root, total_rom_root);
        }

        /**
         * @param listener
         * @param activity
         */
        @SuppressWarnings("deprecation")
        private void getDataRomInfo(Listener listener, Activity activity) {
            File data = Environment.getDataDirectory();
            StatFs data_sf = new StatFs(data.getPath());
            long data_blockSize = data_sf.getBlockSize();
            long data_totalBlocks = data_sf.getBlockCount();
            long data_availableBlocks = data_sf.getAvailableBlocks();
            String total_rom_data = Formatter.formatFileSize(activity.getBaseContext(), data_totalBlocks * data_blockSize);
            String free_rom_data = Formatter.formatFileSize(activity.getBaseContext(), data_availableBlocks * data_blockSize);
            String used_rom_data = Formatter.formatFileSize(activity.getBaseContext(), data_totalBlocks * data_blockSize - data_availableBlocks * data_blockSize);
            listener.IDataRomInfoListener(used_rom_data, free_rom_data, total_rom_data);
        }
    };
}