package com.catchgift.trashclear.networkmonitor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;


import com.catchgift.trashclear.utils.YLog;

import java.util.HashMap;

/**
 * Created by TommyDuan on 16/1/7 at 下午2:33 in Deepclean.
 */
public class MonitorUtils {

    private static final String START_POINT_RX = "start_point_rx";
    private static final String START_POINT_TX = "start_point_tx";
    private static final String START_DATE = "start_date";
    private static final String DATE_LENGTH = "date_length";

    private static final String LIMIT_BYTES = "limit_bytes";
    private static final String SP_NAME = "monitor_net";

    private static Context context;
    private static SharedPreferences sharedPreferences;

    public static void initUtils(Context _context){
        context = _context;
        sharedPreferences = context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE);
    }

    //设置最大值，日期，并重新计数
    public static void setLimit(long bytes,long length){
        YLog.e(TrafficStats.getMobileRxBytes() + "," + TrafficStats.getMobileTxBytes());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        editor.putLong(START_POINT_RX, getRX());
        editor.putLong(START_POINT_TX, getTX());

        editor.putLong(LIMIT_BYTES, bytes);
        editor.putLong(START_DATE, System.currentTimeMillis());
        editor.putLong(DATE_LENGTH, length * 24 * 60 * 60 * 1000);
        editor.apply();
    }

    //获取已用Mobi的流量
    public static HashMap<String, Object> getCurrentMobiUsed(){

        Long srx = sharedPreferences.getLong(START_POINT_RX, -1);
        Long stx = sharedPreferences.getLong(START_POINT_TX, -1);

        if (srx == -1 || stx == -1){
            return  null;
        }

        Long start_date = sharedPreferences.getLong(START_DATE, -1);
        Long date_length = sharedPreferences.getLong(DATE_LENGTH, -1);

        Long limit = sharedPreferences.getLong(LIMIT_BYTES, -1);
        Long date = sharedPreferences.getLong(DATE_LENGTH, -1);

        Float day = (date_length - (System.currentTimeMillis() - start_date)) / (24f * 60f * 60f * 1000f);

        Long urx = getRX() - srx;
        Long utx = getTX() - stx;

        HashMap<String, Object> data = new HashMap<>();
        data.put("rx", urx);
        data.put("tx", utx);
        data.put("li", limit);
        data.put("le", date);
        data.put("dy", day);

        return data;
    }

    public static void cleanup(){
        sharedPreferences.edit().remove(START_POINT_RX);
        sharedPreferences.edit().apply();
    }

    private static long getTX(){
        return TrafficStats.getMobileTxBytes();
    }

    private static  long getRX(){
        return TrafficStats.getMobileRxBytes();
    }
}
