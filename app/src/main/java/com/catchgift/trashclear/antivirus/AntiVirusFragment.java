package com.catchgift.trashclear.antivirus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.catchgift.trashclear.R;
import com.nineoldandroids.view.ViewHelper;

import java.util.List;

/**
 * Created by TommyDuan on 16/1/6 at 上午10:43 in Deepclean.
 */
public class AntiVirusFragment extends Fragment implements View.OnClickListener{

    private Context context;
    private View view;

    private Button virus_back;
    private TextView virus_score;
    private TextView virus_tag;
    private TextView virus_des;
    private TextView virus_show;
    private ImageView iv_scan;

    private RelativeLayout item_trojans;
    private RelativeLayout item_risk;
    private TextView tv_trojans;
    private TextView tv_risk;
    private TextView tv1;
    private TextView tv2;
    private ImageView iv1;
    private ImageView iv2;

    private Boolean isScanning; //是否正在扫描

    private Button virus_bottom;

    private AlphaAnimation  aa;
    private TranslateAnimation ta;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    //获取的应用名
                    String name = (String) msg.obj;
                    //然后给virus_show去展示
                    virus_show.setText(name);
                    break;

                case 1:
                    //扫描正常结束
                    virus_show.setText(R.string.virus_show2);

                    //iv_scan 动画结束、变为透明
                    ViewHelper.setAlpha(iv_scan,0f);
                    iv_scan.clearAnimation();

                    //下半部分文字的颜色改变，按钮变为可以点击
                    int color = context.getResources().getColor(R.color.text_gray);
                    tv_trojans.setTextColor(color);
                    tv_risk.setTextColor(color);

                    tv1.setTextColor(color);
                    tv2.setTextColor(color);

                    iv1.setImageResource(R.drawable.arrow_over);
                    iv2.setImageResource(R.drawable.arrow_over);

                    virus_bottom.setText(R.string.completed);
                    virus_bottom.setTextColor(Color.WHITE);
                    virus_bottom.setBackgroundResource(R.drawable.bg_over_selector);
                    item_trojans.setBackgroundResource(R.drawable.antivirus_item_selecter);
                    item_risk.setBackgroundResource(R.drawable.antivirus_item_selecter);
                    break;
                case 2:
                    //扫描被取消
                    virus_show.setText(R.string.virus_show3);
                    virus_bottom.setText(R.string.startscan);

                    //iv_scan 动画结束、变为透明
                    ViewHelper.setAlpha(iv_scan,0f);
                    iv_scan.clearAnimation();

                    break;
            }



        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        view = View.inflate(context, R.layout.antivirus_fragment, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        initUI();

        setListener();


        super.onActivityCreated(savedInstanceState);
    }


    public void initUI(){
        virus_back = (Button) view.findViewById(R.id.virus_back);
        virus_score = (TextView)view.findViewById(R.id.virus_score);
        virus_tag = (TextView)view.findViewById(R.id.virus_tag);
        virus_des = (TextView)view.findViewById(R.id.virus_des);
        virus_show = (TextView)view.findViewById(R.id.virus_show);
        virus_tag.setText(R.string.virus_tag);  //为了实现换行
        iv_scan = (ImageView) view.findViewById(R.id.iv_scan);
        //透明度先为0
        ViewHelper.setAlpha(iv_scan,0f);

        item_trojans = (RelativeLayout)view.findViewById(R.id.item_trojans);
        item_risk = (RelativeLayout)view.findViewById(R.id.item_risk);
        tv_trojans = (TextView)view.findViewById(R.id.tv_trojans);
        tv_risk = (TextView)view.findViewById(R.id.tv_risk);
        tv1 = (TextView)view.findViewById(R.id.tv1);
        tv2 = (TextView)view.findViewById(R.id.tv2);
        iv1 = (ImageView)view.findViewById(R.id.iv1);
        iv2 = (ImageView)view.findViewById(R.id.iv2);

        virus_bottom = (Button)view.findViewById(R.id.virus_bottom);

    }

    public void setListener(){

        //点击返回按钮
        virus_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                while (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate();
                }
            }
        });


        //点击底部按钮
        virus_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始扫描、停止、完成 三种状态
                String bt_name = virus_bottom.getText().toString().trim();

                String startScan = context.getResources().getString(R.string.startscan);
                String stopScan = context.getResources().getString(R.string.stopscan);
                String completed = context.getResources().getString(R.string.completed);

                if (bt_name.equals(startScan)){
                    isScanning = true;

                    virus_des.setText(R.string.norisk);
                    virus_show.setText(R.string.preparing);
                    virus_bottom.setText(R.string.stopscan);

                    ViewHelper.setAlpha(iv_scan,0.4f);
                    //色块由淡变出
                    if (aa == null){
                        aa = new AlphaAnimation(0, 1.0f);
                        aa.setDuration(1000);
                    }
                    iv_scan.startAnimation(aa);

                    //色块开始扫描的动画
                    if (ta == null){
                        ta = new TranslateAnimation(
                                TranslateAnimation.RELATIVE_TO_SELF, 0,
                                TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                TranslateAnimation.RELATIVE_TO_SELF, 0,
                                TranslateAnimation.RELATIVE_TO_SELF, 1.0f);
                        ta.setRepeatMode(Animation.REVERSE);
                        ta.setRepeatCount(1000);
                        ta.setDuration(1000);
                    }
                    iv_scan.startAnimation(ta);

                    //子线程获取安装应用的名称
                    getInstallApps();

                }else if(bt_name.equals(stopScan)){
                    //仅仅只剩余停止的业务逻辑
                    isScanning = false;

                }else if(bt_name.equals(completed)){
                    //调用返回按钮的点击事件
                    virus_back.performClick();
                }
            }
        });

        //条目的点击事件
        item_trojans.setOnClickListener(this);
        item_risk.setOnClickListener(this);
    }

    /**
     * 两个条目点击事件的实现
     * @param v 点中条目的view对象
     */
    @Override
    public void onClick(View v) {
        //扫描完成之后条目才可以点击
        String bt_name = virus_bottom.getText().toString();  //按钮文字
        String completed = context.getResources().getString(R.string.completed);//完成之后按钮的文字
        if (bt_name.equals(completed)){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction tx = fm.beginTransaction();
            switch (v.getId()) {
                case R.id.item_trojans:
                    //回退栈管理
                    tx.hide(this);
                    tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    tx.add(R.id.main_content, new TrojansFragment(), "TROJANS");
                    tx.addToBackStack(null);
                    tx.commit();
                    break;

                case R.id.item_risk:
                    tx.hide(this);
                    tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    tx.add(R.id.main_content, new RiskAppFragment(), "RISKAPP");
                    tx.addToBackStack(null);
                    tx.commit();
                    break;

            }
        }
    }


    /**
     * 开子线程获取安装的应用名
     */
    public void getInstallApps(){
        new Thread(){
            @Override
            public void run() {
                //获取所有的已经安装的应用
                PackageManager pm = context.getPackageManager();
                List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
                for (PackageInfo packageInfo : installedPackages){
                    if (isScanning){
                        String packgeName = packageInfo.packageName;
                        String appName;

                        try {
                            ApplicationInfo applicationInfo = pm.getApplicationInfo(packgeName, 0);
                            //名称
                            appName = applicationInfo.loadLabel(pm).toString();

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();

                            appName = packgeName;
                        }

                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = appName;
                        mHandler.sendMessage(msg);

                        try {
                            //休眠时间是个随机数
                            Thread.sleep(20+(int)(Math.random()*20));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (isScanning){ //正常结束
                    Message msg1 = Message.obtain();
                    msg1.what = 1;
                    mHandler.sendMessage(msg1);
                }else{           //被取消
                    Message msg2 = Message.obtain();
                    msg2.what = 2;
                    mHandler.sendMessage(msg2);
                }

            }
        }.start();

    }

}
