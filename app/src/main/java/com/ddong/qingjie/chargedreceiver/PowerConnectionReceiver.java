package com.ddong.qingjie.chargedreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import com.ddong.qingjie.ads.Config;
import com.ddong.qingjie.bootreceiver.CTService;

public class PowerConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.ACTION_POWER_CONNECTED")) {

            //注册监听电池电量变化的广播【不是一直变化，第一次的时候可以用这个】
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus;
            batteryStatus = context.registerReceiver(null, ifilter);

            //获取充电状态（是否在充电）
            //int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            //boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ;

            //当前剩余电量
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            //电量最大值
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            //未冲电量的百分
            float surplusBattery = (scale - level) * 1.0f / scale;

            //判断充电方式
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;    //USB充电
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;    //AC直充

            float surplusTime = 0.0f;

            /**
             *  第一次进来，电池的总量假设为1500毫安,
             USB默认充电速率为500毫安/小时
             AC直冲默认充电速率为1000毫安/小时
             充满电的时间为总的毫安数除以充电的速率(每小时冲多少电流);
             */

            if (usbCharge) {
                float totalTime = (float) (1500 / 500) * 60;        //单位分钟
                surplusTime = totalTime * surplusBattery;            //剩余充电时间

            } else if (acCharge) {
                float totalTime = (float) (1500 / 1000) * 60;      //单位分钟
                surplusTime = totalTime * surplusBattery;            //剩余充电时间
            }

            //打开充电界面
            Intent startIntent = new Intent(context, ChargedActivity.class);
            startIntent.putExtra("level", level);
            startIntent.putExtra("surplusTime", surplusTime);
            startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startIntent);
        }
    }
}