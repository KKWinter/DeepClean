package com.catchgift.trashclear.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.catchgift.shell.ShellManager;
import com.catchgift.trashclear.R;
import com.catchgift.trashclear.utils.ToolUtils;
import com.catchgift.trashclear.appfragment.UserFragment;
import com.catchgift.trashclear.bootreceiver.CTService;
import com.catchgift.trashclear.fragment.MainFragment;
import com.catchgift.trashclear.networkmonitor.MonitorUtils;
import com.catchgift.trashclear.notification.NoticeService;
import com.catchgift.trashclear.utils.BroadcastUtils;
import com.facebook.appevents.AppEventsLogger;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import static com.android.installreferrer.api.InstallReferrerClient.newBuilder;


public class MainActivity extends FragmentActivity implements InstallReferrerStateListener{

    private Context context;

    public  static String TAG = MainActivity.class.getName().toString();
    public static InstallReferrerClient mReferrerClient;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        mReferrerClient = newBuilder(this).build();
        mReferrerClient.startConnection(this);


        //设置activity的通知栏背景色
        ToolUtils.setStatusBar(this);

        //友盟自动更新
        UmengUpdateAgent.setUpdateOnlyWifi(false);  //设置非wifi下也可以下载
        UmengUpdateAgent.silentUpdate(this);        //设置自动静默检测更新下载，通知栏提示安装

        //友盟数据统计
        MobclickAgent.setScenarioType(context, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent. startWithConfigure(new MobclickAgent.UMAnalyticsConfig(context,"5645413667e58efd78006185","gp_special_20161101"));

        //activity的布局
        setContentView(R.layout.main_activity_layout);

        //初始化流量工具
        MonitorUtils.initUtils(this);

        //调用广播工具类，注册广播，获取数据
        BroadcastUtils.register(this);

        //展示主界面mainfragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.main_content, new MainFragment(), "HOME");
        ft.commit();

        //无条件启动CTService
        Intent intent1 = new Intent(this, CTService.class);
        this.startService(intent1);


        //根据是否开启通知栏开关状态，启动NoticeService
        SharedPreferences sp = context.getSharedPreferences("Notice", Context.MODE_PRIVATE);
        boolean isNotice = sp.getBoolean("IsNotice", true);
        if (isNotice){
            Intent intent2 = new Intent(context, NoticeService.class);
            context.startService(intent2);
        }

        //西安sdk
        ShellManager.getInstance().init(context,"app_deep_clean_newera_6666");


    }


    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {


        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                try {
                    Log.v(TAG, "InstallReferrer conneceted");
                    ReferrerDetails response = mReferrerClient.getInstallReferrer();
                    handleReferrer(response);
                    mReferrerClient.endConnection();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                Log.w(TAG, "InstallReferrer not supported");
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                Log.w(TAG, "Unable to connect to the service");
                break;
            default:
                Log.w(TAG, "responseCode not found.");
        }
    }

    public void handleReferrer(ReferrerDetails response) {

        long installBeginTimestampSeconds = response.getInstallBeginTimestampSeconds();

        Log.e(TAG,installBeginTimestampSeconds+"");
        long referrerClickTimestampSeconds = response.getReferrerClickTimestampSeconds();
        Log.e(TAG,referrerClickTimestampSeconds+"");
        String installReferrer = response.getInstallReferrer();
        Log.e(TAG,installReferrer+"");
    }

    @Override
    public void onInstallReferrerServiceDisconnected() {

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Adjust.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {

        super.onResume();
//        Adjust.onResume();
        MobclickAgent.onResume(this);
        AppEventsLogger.activateApp(this);

    }

    @Override
    protected void onDestroy() {
        //注销广播
        BroadcastUtils.unregister(this);
        //子线程也一起停止
        System.exit(0);
        super.onDestroy();
    }

    //用于appFragment中的用户应用界面
    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        //获取卸载界面关闭之后的时间标示，然后通知userFragment去刷新数据
        UserFragment.notifyUpdate();
        super.onActivityResult(arg0, arg1, arg2);
    }

    //
    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }


}
