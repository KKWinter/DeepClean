package com.catchgift.trashclear.sys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    int i = 0;
    private Listener listener = null;

    public interface Listener {
        public void BatteryListener(String health, String status,
                                    float voltage, int current, int total, double temperature);
    }

    public BatteryBroadcastReceiver(Listener _listener) {
        listener = _listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

            int status = intent.getIntExtra("status", 0);                       //充电状态
            int health = intent.getIntExtra("health", 0);                       //电池状态

            float voltage = intent.getIntExtra("voltage", 0);                   //电压
            int current = intent.getIntExtra("level", 0);                       //当前电量
            int total = intent.getExtras().getInt("scale");                     //总电量
            double temperature = intent.getIntExtra("temperature", 0) * 0.1;    //温度

            String statusString = "";
            switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    statusString = "Nnknown";                                   //未知状态
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusString = "Charging";                                  //充电状态
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusString = "Discharging";                               //放电状态
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    statusString = "Not charging";                              //未充电状态
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    statusString = "Full";                                      //充满电状态
                    break;
            }

            //电池状态
            String healthString = "";
            switch (health) {
                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                    healthString = "Nnknown";                                   //未知问题
                    break;
                case BatteryManager.BATTERY_HEALTH_GOOD:
                    healthString = "Good";                                      //状态良好
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    healthString = "Overheat";                                  //电池过热
                    break;
                case BatteryManager.BATTERY_HEALTH_DEAD:
                    healthString = "Dead";                                      //电池没有电
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    healthString = "Voltage";                                   //电池电压过高
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    healthString = "Unspecified failure";
                    break;
            }

            listener.BatteryListener(healthString, statusString,
                    voltage, current, total, temperature);
        }
    }
}