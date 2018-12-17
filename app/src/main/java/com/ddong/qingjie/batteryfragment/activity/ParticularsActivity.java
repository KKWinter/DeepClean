package com.ddong.qingjie.batteryfragment.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ddong.qingjie.R;
import com.ddong.qingjie.batteryfragment.BattRankInfo;
import com.ddong.qingjie.batteryfragment.DrawableToString;
import com.ddong.qingjie.sql.ExceptionDao;
import com.ddong.qingjie.utils.ToastUtils;
import com.ddong.qingjie.utils.UMengStaticValue;
import com.ddong.qingjie.view.MonitorProgressBar;

import com.ddong.qingjie.appfragment.ApkUninstallUtil;
import com.umeng.analytics.MobclickAgent;

//import android.annotation.SuppressLint;

public class ParticularsActivity extends Activity {

    private Context mContext;

    private Button power_detail_back;
    private MonitorProgressBar particulars_battery;
    private ImageView particulars_image;
    private TextView particulars_name;

    private Button particulars_uninstall;
    private Button particulars_see;

    private TextView particulars_cpu;

    private RelativeLayout exception_relativelayout;
    private static ImageView exception_image;

    //判断是否开启监听
    private static boolean isOpen = false;

    //接受sql查询的集合
    private BattRankInfo sipper;
    private ExceptionDao mExceptionDao;

    public static Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (isOpen) {
                exception_image.setImageResource(R.drawable.switch_open);
            } else {
                exception_image.setImageResource(R.drawable.switch_close);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particulars);
        mContext = this;

        sipper = getIntent().getParcelableExtra("BatterySipper");  //parcelable序列化
        mExceptionDao = ExceptionDao.getInstance(mContext);

        initUI();
        initData();
        setListener();
    }

    private void initUI() {
        //返回按钮
        power_detail_back = (Button) findViewById(R.id.power_detail_back);
        //应用详情
        particulars_image = (ImageView) findViewById(R.id.particulars_image);
        particulars_name = (TextView) findViewById(R.id.particulars_name);
        particulars_battery = (MonitorProgressBar) findViewById(R.id.particulars_battery);
        //卸载和查看按钮
        particulars_uninstall = (Button) findViewById(R.id.particulars_uninstall);
        particulars_see = (Button) findViewById(R.id.particulars_see);
        //cpu占用
        particulars_cpu = (TextView) findViewById(R.id.particulars_cpu);
        //异常耗电提醒
        exception_relativelayout = (RelativeLayout) findViewById(R.id.exception_relativelayout);
        exception_image = (ImageView) findViewById(R.id.exception_image);
    }

    private void initData() {
        particulars_image.setImageDrawable(DrawableToString.byteToDrawable(sipper.getBitmap()));
        particulars_name.setText(sipper.getName());

        String particulars_battery_txt = mContext.getResources().getString(R.string.consumption)
                + (Math.round(sipper.getPercentOfTotal()) / 1.00) + "" + "%";
        particulars_battery.setProgress((int)sipper.getPercentOfTotal());
        particulars_battery.setText(particulars_battery_txt);

        double cpuTime = sipper.getValue();
        double time;
        String cpu_txt;
        if (cpuTime >= 1000) {
            time = cpuTime * 0.001;
            cpu_txt = (Math.round(time) / 1.00) + mContext.getResources().getString(R.string.fen);
        } else {
            cpu_txt = cpuTime + mContext.getResources().getString(R.string.millisecond);
        }
        particulars_cpu.setText(cpu_txt);

        new Thread() {
            public void run() {
                //根据在数据库中能否查看到，确定UI中开关是否打开
                isOpen = mExceptionDao.findName(sipper);
                mHandler.sendEmptyMessage(0);
            }

        }.start();
    }

    private void setListener() {
        power_detail_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        particulars_uninstall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sipper.getPkgName() == null) {
                    String notify = mContext.getResources().getString(R.string.notify);
                    ToastUtils.show(mContext, notify);
                } else {
                    ApkUninstallUtil.uninstall(sipper.getPkgName(), mContext);
                }
            }
        });

        particulars_see.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sipper.getPkgName() == null) {
                    //系统应用查看不了，写一个toast文本
                    String see = mContext.getResources().getString(R.string.see);
                    ToastUtils.show(mContext, see);
                } else {
                    Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.parse("package:" + sipper.getPkgName()));
                    startActivity(intent);
                }
            }
        });

        exception_relativelayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    mExceptionDao.delete(sipper);
                    exception_image.setImageResource(R.drawable.switch_close);

                    isOpen = false;
                } else {
                    mExceptionDao.insert(sipper);
                    exception_image.setImageResource(R.drawable.switch_open);

                    isOpen = true;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Adjust.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(UMengStaticValue.BATT_ACT_PART);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Adjust.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(UMengStaticValue.BATT_ACT_PART);
    }

}
