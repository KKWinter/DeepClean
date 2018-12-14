package com.catchgift.trashclear.utils;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;

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

    }

    public static Context getContext(){
        return context;
    }


}
