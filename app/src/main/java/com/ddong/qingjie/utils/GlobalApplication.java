package com.ddong.qingjie.utils;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.ddong.qingjie.ads.Config;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.zcoup.base.config.Const;
import com.zcoup.base.core.ZcoupSDK;

/**
 * Created by Antony on 2016/1/25.
 */
public class GlobalApplication extends Application{


    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        //初始化facebooksdk
        FacebookSdk.sdkInitialize(getApplicationContext());
        Fresco.initialize(context);
        ZcoupSDK.initialize(context, Config.slotId_native);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(context);
    }

    public static Context getContext(){
        return context;
    }


}
