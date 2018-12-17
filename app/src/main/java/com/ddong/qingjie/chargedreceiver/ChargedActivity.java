package com.ddong.qingjie.chargedreceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.ddong.qingjie.R;
import com.ddong.qingjie.ads.Config;
import com.ddong.qingjie.ads.DAdEventListener;
import com.ddong.qingjie.utils.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;
import com.zcoup.base.core.ZCAdvanceNative;
import com.zcoup.base.core.ZCNative;
import com.zcoup.base.core.ZcoupSDK;
import com.zcoup.base.enums.AdSize;
import com.zcoup.base.utils.ContextHolder;

/**
 * Created by huangdong on 16/8/31.
 */
public class ChargedActivity extends Activity {

    private RelativeLayout rl_container;
    private ProgressView progressView;

    private IntentFilter mIntentFilter;                   //监听电池电量变化的广播
    private IntentFilter filter;                          //监听断开充电连接的广播

    private int initLevel;                            //初始的电量
    private long initTime;                            //初始的时间
    private ViewGroup adLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charged_usb);

        //初始化adjust
        String appToken = "9v7ehl9bnhts";
        //String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        Adjust.onCreate(config);


        progressView = (ProgressView) findViewById(R.id.progressView);
        rl_container = (RelativeLayout) findViewById(R.id.rl_container);

        //界面底部获取广告
        initAd();

        //第一次插入的时候
        FirstEnter();

        //然后注册监听电池变化的广播
        mIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //注册拔出充电连接的广播
        filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
    }


    private void initAd() {
        adLayout = (ViewGroup) View.inflate(ContextHolder.getGlobalAppContext(), R.layout.advance_native_layout, null);

        ZcoupSDK.getNativeAd(Config.slotId_native,
                ContextHolder.getGlobalAppContext(), new DAdEventListener(){
                    @Override
                    public void onReceiveAdSucceed(ZCNative zcNative) {
                        super.onReceiveAdSucceed(zcNative);
                        showAd((ZCAdvanceNative) zcNative);
                    }
        });
    }


    private void showAd(ZCAdvanceNative ctAdvanceNative) {

        SimpleDraweeView img =  adLayout.findViewById(R.id.iv_img);
        SimpleDraweeView icon =  adLayout.findViewById(R.id.iv_icon);
        TextView title =  adLayout.findViewById(R.id.tv_title);
//        TextView desc =  adLayout.findViewById(R.id.tv_desc);
//        TextView click =  adLayout.findViewById(R.id.bt_click);
        SimpleDraweeView ad_choice_icon = adLayout.findViewById(R.id.ad_choice_icon);

        img.setImageURI(Uri.parse(ctAdvanceNative.getImageUrl()));
        icon.setImageURI(Uri.parse(ctAdvanceNative.getIconUrl()));
        title.setText(ctAdvanceNative.getTitle());
//        desc.setText(ctAdvanceNative.getDesc());
//        click.setText(ctAdvanceNative.getButtonStr());
        ad_choice_icon.setImageURI(ctAdvanceNative.getAdChoiceIconUrl());

        rl_container.removeAllViews();
        rl_container.setBackgroundColor(Color.WHITE);

        //只是注册点击区域,调此处设置
        ctAdvanceNative.registeADClickArea(adLayout);
        rl_container.addView(adLayout);
    }


    private void FirstEnter() {
        Intent intent = getIntent();
        int level = intent.getIntExtra("level", 100);
        float surplusTime = intent.getFloatExtra("surplusTime", 0); //剩余充电时间，单位分钟

        initLevel = level;
        initTime = System.currentTimeMillis();

        //初始电量,初始需要时间
        progressView.setProgress(initLevel, mathabs(surplusTime));

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, filter);
        registerReceiver(mIntentReceiver, mIntentFilter);
        Adjust.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Adjust.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        unregisterReceiver(mIntentReceiver);
        progressView.destoryRotate();
        super.onDestroy();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ChargedActivity.this.finish();
        }

    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        private float perMinute = 0;

        @Override
        public void onReceive(Context context, Intent intent) {

            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 0);

            if (level == 100) {
                progressView.setProgress(100, "over");
                return;
            }

            //判断充电方式
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;               //USB充电
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;                 //AC直充

            //根据变化的1%计算
            if (level - initLevel == 1) {
                //变化之后的时间
                long currentTime = System.currentTimeMillis();
                //变化的时间差
                long addTime = currentTime - initTime;          //单位毫秒
                //充电1%所需要的时间
                perMinute = addTime / (1000 * 60);
            }

            if (perMinute != 0) {
                //剩余要充电的比例
                int surplusVolume = scale - level;
                //剩余要充电的时间
                float surplusTime = surplusVolume * perMinute;  //分钟

                //TODO
                progressView.setProgress(level, mathabs(surplusTime));

//                //充满电所需要的时间
//                float allTime = 100 * perMinute;
//                if (usbCharge) {
//                    //如果是USB充电，电池的容量为：
//                    int volume = (int) (500 * (allTime / 60));
//                } else if (acCharge) {
//                    //如果是AC充电，电池的容量为：
//                    int volume = (int) (1000 * (allTime / 60));
//                }
            }

        }
    };

    //min单位是分钟
    private String mathabs(float minute) {
        String hr = Utils.getString(R.string.hr);
        String min = Utils.getString(R.string.min);

        if (minute < 60) {
            return Math.round(minute) + min;
        } else {
            int h = (int) (minute / 60);
            int m = (int) (minute % 60);
            return h + hr + " " + m + min;
        }
    }


}
