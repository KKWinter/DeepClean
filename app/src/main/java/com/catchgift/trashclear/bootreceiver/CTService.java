package com.catchgift.trashclear.bootreceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.catchgift.trashclear.R;
import com.catchgift.trashclear.batteryfragment.BattRankInfo;
import com.catchgift.trashclear.batteryfragment.BattRankInfoProvider;
import com.catchgift.trashclear.batteryfragment.activity.ParticularsActivity;
import com.catchgift.trashclear.mainfragment.ProcessInfoProvider;
import com.catchgift.trashclear.sql.ExceptionDao;
import com.catchgift.trashclear.utils.UMengStaticValue;

import com.catchgift.trashclear.utils.SaveUtils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 后台服务
 */
public class CTService extends Service {
    private ExceptionDao mExceptionDao;
    private Context context;

    //获取开关的开启状态
    private boolean isStart = false;

    @Override
    public void onCreate() {
        context = getApplicationContext();

        //注册监听锁屏的广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenReceiver, intentFilter);

        MobclickAgent.onEvent(this, UMengStaticValue.SERVICE_KILL_PROCESS);

        //杀死后台进程任务的定时器，一直开启，根据开关状态判断是否执行
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            public void run() {
                if (isStart) {
                    //开始杀进程
                    ProcessInfoProvider.killProcess(context);
                }
            }
        };
        timer.schedule(task, 0, 5 * 60 * 1000);

        //监听异常耗电的定时
        mExceptionDao = ExceptionDao.getInstance(context);
        Timer listenertimer = new Timer(true);
        TimerTask listenertask = new TimerTask() {
            @Override
            public void run() {
                ExceptionReminderListener();
            }
        };
        listenertimer.schedule(listenertask, 0, 10 * 60 * 1000);


        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    //锁屏开屏的广播
    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {              //锁屏
                //获取私有目录下保存的是否开启的状态
                isStart = SaveUtils.getUserInfo(context);
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {        //开屏
                isStart = false;
            }
        }
    };

    private synchronized void ExceptionReminderListener() {
        List<BattRankInfo> list = new ArrayList<>();
        list.addAll(mExceptionDao.findAll());
        if (list.size() != 0) {
            BattRankInfoProvider info = new BattRankInfoProvider(this);
            ArrayList<BattRankInfo> allList = (ArrayList<BattRankInfo>) info.getBatteryStats();

            for (int i = 0; i < list.size(); i++) {
                BattRankInfo batterySipper = list.get(i);

                for (BattRankInfo battery : allList) {
                    if (battery.getName().equals(batterySipper.getName())) {
                        if (battery.getPercentOfTotal() > 20) {
                            notityMe(battery, i);
                        }
                    }
                }
            }
        }
    }

    //推送消息
    @SuppressWarnings("deprecation")
    private synchronized void notityMe(BattRankInfo battery, int index) {
        // 获得通知管理器，通知是一项系统服务
        Notification notification = new Notification(R.drawable.logo2,
                context.getResources().getString(R.string.exception_text), System.currentTimeMillis());

        Intent intent = new Intent(context, ParticularsActivity.class);
        intent.putExtra("BatterySipper", battery);
        PendingIntent pintentIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        //TODO
//        notification.setLatestEventInfo(this, battery.getName(), context.getResources().getString(R.string.exception), pintentIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(index, notification);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
