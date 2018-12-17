package com.ddong.qingjie.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.ddong.qingjie.activity.BatteryDataInfo;
import com.ddong.qingjie.sys.BatteryBroadcastReceiver;

/**
 * Created by Antony on 2016/1/22.
 */
public class BroadcastUtils {
    private static BatteryBroadcastReceiver batteryBroadcastReceiver;

    /**
     * 注册电池的广播，获取电池数据
     * @param context contexxt
     */
    public static void register(final Context context){
            //注册监听电池温度的广播
            batteryBroadcastReceiver = new BatteryBroadcastReceiver(new BatteryBroadcastReceiver.Listener() {
                @Override
                public void BatteryListener(String health, String status,
                                            float voltage, int current, int total, double temperature) {
                    temperature = (double) Math.round(temperature * 100) / 100;
                    voltage = (float) (Math.round(voltage * 0.001) / 1.0);

                    BatteryDataInfo batteryInfo = BatteryDataInfo.getBatteryInfo();
                    batteryInfo.temperature = temperature;
                    batteryInfo.voltage = voltage + "V";
                    batteryInfo.current = current;
                    batteryInfo.total = total;

                    SharedPreferences sp = context.getSharedPreferences("NoticeTem", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putInt("tem", (int) temperature);
                    edit.putInt("curr",current);
                    edit.commit();

                }
            });

            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(batteryBroadcastReceiver, filter);

    }

    /**
     * 注销电池的广播
     * @param context context
     */
    public static void unregister(Context context){
        //注销广播
        context.unregisterReceiver(batteryBroadcastReceiver);
    }


}
