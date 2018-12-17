package com.ddong.qingjie.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.ddong.qingjie.R;
import com.ddong.qingjie.activity.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Antony on 2016/1/7.
 */
public class ToolUtils {

    /**
     * 获取转换为MB的大小
     */
    public static int getSize(long size){

        float temp = (float) (size*1.0/(1024*1024));
        if (temp > 0){
            temp = temp + 1.0f;
        }
        int result = (int)Math.floor(temp);

        return result;
    }

    /**
     * 获取所有安装的用户应用
     */
    public static List<PackageInfo> getInstalledUserApps(Context context){
        List<PackageInfo> userAppList = new ArrayList<>();

        PackageManager pm = context.getPackageManager();

        //获取所有的已经安装的应用
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {
            String packgeName = packageInfo.packageName;
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packgeName, 0);
                //判读是否用户应用
                if((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM){
                    userAppList.add(packageInfo); //所有的用户应用
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();

            }
        }
        return userAppList;
    }

    /**
     * 获取手机是否连接mobile网络
     * @param context context
     * @return boolean
     */
    public static boolean getNetworkInfo(Context context){
        //判断TYPE_MOBILE是否可用，以及当前连接的网络是否TYPE_MOBILE
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        //手机卡流量可用且开启
        if (mNetworkInfo != null && mNetworkInfo.getType()==0 ){
            return true;
        }
        return false;
    }

    /**
     * 从一串字符串中取出其中的数
     * @param str 传入的字符串
     * @return  返回的字符串中的数【包括小数点】
     */
    public static int getNum(String str){
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        String ss = m.replaceAll("").trim();

        return Integer.valueOf(ss);
    }


    /**
     * 是否开始流量设置的对话框
     * @param context
     */
    public static void setNetworkMethod(final Context context){
        String dialog1 = context.getResources().getString(R.string.dialog1);
        String dialog2 = context.getResources().getString(R.string.dialog2);
        String dialog3 = context.getResources().getString(R.string.dialog3);
        String dialog4 = context.getResources().getString(R.string.dialog4);

        //提示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(dialog1).setMessage(dialog2).setPositiveButton(dialog3, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                //判断手机系统的版本  即API大于10 就是3.0或以上版本
                if(android.os.Build.VERSION.SDK_INT>10){
                    intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                }else{
                    intent = new Intent();
                    ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
                    intent.setComponent(component);
                    intent.setAction("android.intent.action.VIEW");
                }
                context.startActivity(intent);
            }
        }).setNegativeButton(dialog4, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 为activity设置通知栏条目无色透明
     * @param activity activity对象
     */
    public static void setStatusBar(Activity activity){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(activity, true);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);

        tintManager.setStatusBarTintEnabled(true);

        tintManager.setStatusBarTintResource(R.color.transparent);

    }



    @TargetApi(19)

    private static void setTranslucentStatus(Activity activity, boolean on) {

        Window win = activity.getWindow();

        WindowManager.LayoutParams winParams = win.getAttributes();

        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

        if (on) {

            winParams.flags |= bits;

        } else {

            winParams.flags &= ~bits;

        }

        win.setAttributes(winParams);

    }







}
