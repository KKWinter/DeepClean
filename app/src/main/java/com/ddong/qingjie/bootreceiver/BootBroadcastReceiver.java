package com.ddong.qingjie.bootreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 接收开机广播
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Intent serviceIntent = new Intent(context, CTService.class);
            context.startService(serviceIntent);
        }
    }
}