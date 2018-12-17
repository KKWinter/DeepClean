package com.ddong.qingjie.notification;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.ddong.qingjie.R;
import com.ddong.qingjie.activity.BatteryDataInfo;
import com.ddong.qingjie.fragment.BatteryFragment;
import com.ddong.qingjie.networkmonitor.MonitorFragment;
import com.ddong.qingjie.sys.BatteryBroadcastReceiver;
import com.ddong.qingjie.utils.ToolUtils;

import java.util.ArrayList;

/**
 * Created by Antony on 2016/1/22.
 */
public class NoticeActivity extends FragmentActivity {


    private ArrayList<Fragment> fragmentList;
    private Context context;
    private BatteryBroadcastReceiver battBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置activity的状态栏
        ToolUtils.setStatusBar(this);
        setContentView(R.layout.notice_activity_layout);

        context = this;
        //再注册广播获取数据
        //注册监听电池温度的广播
        battBroadcastReceiver = new BatteryBroadcastReceiver(new BatteryBroadcastReceiver.Listener() {
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
        context.registerReceiver(battBroadcastReceiver, filter);


        Intent intent = getIntent();
        int page = intent.getIntExtra("page",0);
        show(page);

    }

    /**
     * 再次打开之后调用这个方法
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        int page = intent.getIntExtra("page",0);
        show(page);

        super.onNewIntent(intent);
    }

    //定义一个方法，根据传进来的参数实现显示和隐藏
    public void show(int index){
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        switch (index){
            case 1:
                ft.replace(R.id.notice_layout,new MonitorFragment());
                break;
            case 2:
                ft.replace(R.id.notice_layout,new BatteryFragment());
                break;
        }
        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Adjust.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Adjust.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        context.unregisterReceiver(battBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


}
