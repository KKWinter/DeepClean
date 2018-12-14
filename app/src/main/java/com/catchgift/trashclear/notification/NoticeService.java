package com.catchgift.trashclear.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.text.format.Formatter;
import android.widget.RemoteViews;

import com.catchgift.trashclear.R;
import com.catchgift.trashclear.activity.MainActivity;
import com.catchgift.trashclear.mainfragment.ProcessInfoProvider;
import com.catchgift.trashclear.utils.ToolUtils;
import com.catchgift.trashclear.networkmonitor.MonitorUtils;

import java.util.HashMap;

/**
 * Created by Antony on 2016/1/20.
 */
public class NoticeService extends Service {

    private NotificationManager manager;
    private Context context;
    private boolean flow;     //是否开启流量
    private String flow_str;  //未设置流量/流量已使用
    private int mem_per;      //内存占用百分比
    private double temperature;  //温度
    private int total;           //总电量
    private int current;         //当前电量

    private Bitmap bitmap;  //转换的bitmap对象
    @Override
    public void onCreate() {
        context = getApplicationContext();

        //注册电池广播
        //然后注册监听电池变化的广播
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mIntentReceiver, mIntentFilter);

        super.onCreate();
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //温度和电量
            current = intent.getIntExtra("level", 0);
            total = intent.getExtras().getInt("scale");
            temperature = intent.getIntExtra("temperature", 0) * 0.1;

            //存起来
            SharedPreferences sp = context.getSharedPreferences("NoticeTem", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putInt("tem", (int) temperature);
            edit.putInt("curr", current);
            edit.commit();


            //准备数据—流量、内存、温度、电量、
            //流量
            Boolean isMobileType = ToolUtils.getNetworkInfo(context);
            if (isMobileType){   //开启手机卡流量
                //获取设置的数据流量
                HashMap<String, Object> data = MonitorUtils.getCurrentMobiUsed();
                if (data == null){
                    flow = false;
                }else{
                    flow = true;

                    //已经设置数据
                    long rx = (Long) data.get("rx"); //接收字节
                    long tx = (Long) data.get("tx"); //发送字节
                    long li = (Long) data.get("li"); //流量限制，即总流量
                    long le = (Long) data.get("le"); //设置时间长度
                    float day = (Float) data.get("dy"); //剩余日期

                    //已经使用
                    long total_flow = rx + tx;
                    flow_str = Formatter.formatFileSize(context, total_flow);
                }
            }else{
                flow = false;
            }

            //内存
            long availSpace = ProcessInfoProvider.getAvailSpace(context);
            long totalSpace = ProcessInfoProvider.getTotalSpace();
            mem_per = (int) ((totalSpace - availSpace)*100/totalSpace);

            //打开notification通知的方法
            StatusBarNofity();

        }
    };

    public void StatusBarNofity(){
        //获取通知管理者对象
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //创建通知对象
        Notification notification = new Notification();
        notification.icon = R.drawable.logo2;
        notification.tickerText = context.getResources().getString(R.string.tickerText);
        notification.when = System.currentTimeMillis();

        //自定义通知的布局
        RemoteViews contentView = new RemoteViews(getPackageName(),R.layout.notification_layout);
        //先设置三个固定不变的内容
        contentView.setTextViewText(R.id.tv_used,context.getResources().getString(R.string.used));
        contentView.setTextViewText(R.id.tv_memused,context.getResources().getString(R.string.usedmem));
        contentView.setTextViewText(R.id.tv_savepower,context.getResources().getString(R.string.saveelec));

        //设置流量
        if (flow){
            contentView.setTextViewText(R.id.tv_notice_flow,flow_str);
        }else{
            contentView.setTextViewText(R.id.tv_notice_flow,context.getResources().getString(R.string.nosetting));
        }

        //设置温度和电量
        contentView.setTextViewText(R.id.tv_notice_bat,current+"%");
        contentView.setTextViewText(R.id.tv_notice_tem, (int) (temperature + 0.5) + "℃");

        //设置内存占用
        PBDrawable pbDrawable = new PBDrawable(context,mem_per);
        drawableToBitamp(pbDrawable);
        contentView.setImageViewBitmap(R.id.iv_no,bitmap);
        contentView.setTextViewText(R.id.tv_per,mem_per + "%");

        //默认跳转到主界面
        Intent intent0 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //自定义点击按钮的跳转
        Intent intent1 = new Intent(context, NoticeActivity.class);
        intent1.putExtra("page", 1);
        PendingIntent pIntentButton1 = PendingIntent.getActivity(context, 1, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.ll_notice_f, pIntentButton1);

        Intent intent2 = new Intent(context,NoticeActivity.class);
        intent2.putExtra("page", 2);
        PendingIntent pIntentButton2 = PendingIntent.getActivity(context, 2, intent2,
                PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.ll_notice_e, pIntentButton2);

        //设置属性
        notification.flags = Notification.FLAG_NO_CLEAR;
        notification.flags = Notification.FLAG_ONGOING_EVENT; // 设置常驻，不能取消
        notification.priority = Notification.PRIORITY_DEFAULT;

        //添加到通知中
        notification.contentView = contentView;
        notification.contentIntent = pendingIntent;

        manager.notify(1,notification);

    }

    private void drawableToBitamp(Drawable drawable){
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        bitmap = Bitmap.createBitmap(w,h,config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //关闭通知
        manager.cancel(1);
        //注销广播
        unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
