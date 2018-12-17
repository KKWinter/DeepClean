package com.ddong.qingjie.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ddong.qingjie.R;
import com.ddong.qingjie.activity.BatteryDataInfo;
import com.ddong.qingjie.antivirus.AntiVirusFragment;
import com.ddong.qingjie.mainfragment.AppCacheInfo;
import com.ddong.qingjie.mainfragment.YiBiaoView;
import com.ddong.qingjie.sys.CleanAppCache;
import com.ddong.qingjie.utils.ToolUtils;
import com.ddong.qingjie.utils.UMengStaticValue;
import com.ddong.qingjie.mainfragment.ProcessInfo;
import com.ddong.qingjie.networkmonitor.MonitorFragment;
import com.ddong.qingjie.utils.ConstantValues;
import com.nineoldandroids.animation.ObjectAnimator;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements View.OnClickListener {
    private View view;
    private Context context;
    private PackageManager pm;

    //上半部分
    private YiBiaoView ybv;
    private Button bt_start;
    private TextView tv_des;

    //下半部分
    private LinearLayout ll_button;//最外层
    private Button bt_speed;
    private Button bt_battery;
    private Button bt_app;
    private Button bt_virus;
    private Button bt_flow;
    private Button bt_more;

    private LinearLayout ll_major; //最外层
    private LinearLayout ll_parent;
    private Button bt_cancel;

    //最后一个界面
    private LinearLayout ll_main_ybv;
    private RelativeLayout rl_main;

    private RelativeLayout rl_result_fen;
    private RelativeLayout rl_result_item;

    private TextView fen;
    private TextView tv_mem;
    private TextView tv_rub;
    private TextView tv_tem;
    private Button bt_over;

    //休眠时间
    private long MAJOR_SLEEP = 30; //检测时，分数递减的时间间隔
    private long CLEAN_SLEEP = 30; //优化时，条目展示的时间间隔

    //子线程控制开关
    private boolean isCheck = true;  //控制检测的子线程
    private boolean isMajor = true;  //控制优化的子线程

    //检测的异步handler
    private ArrayList<ProcessInfo> processList;
    private ArrayList<AppCacheInfo> cacheList;
    private List<PackageInfo> userAppList;
    private int sumScroe = 100;//初始的总分数
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        private int mm;
        public void handleMessage(android.os.Message msg){
            switch (msg.what) {
                case 1:

                    //手机温度分数的扣减
                    int temKou = (int) msg.obj;
                    ybv.kouScore(temKou);

                    break;
                case 2:
                    //用户应用进程
                    ProcessInfo processInfo = (ProcessInfo) msg.obj;
                    //每一个的大小
                    int processSize = ToolUtils.getSize(processInfo.itemSize);  //转换为MB的大小
                    //根据系数计算扣减分数
                    int processScore = (int) (processSize * ConstantValues.PROCESSPER);
                    ybv.kouScore(processScore);

                    //最后获取数据集合，供以后使用
                    if (!processList.contains(processInfo)) {
                        processList.add(processInfo);
                    }
                    break;

                case 3:
                    //用户应用缓存
                    AppCacheInfo appCacheInfo = (AppCacheInfo) msg.obj;


                    //每一个的大小
                    int cacheSize = ToolUtils.getSize(appCacheInfo.cacheSize);  //转换为MB的大小
                    //根据系数得到扣减的分数
                    int cacheScore = (int) (cacheSize * ConstantValues.CACHEPER);
                    ybv.kouScore(cacheScore);


                    //最后获取数据集合，供以后使用
                    if (!cacheList.contains(appCacheInfo)) {
                        cacheList.add(appCacheInfo);
                    }


                    //判断获取应用缓存是否结束
                    if(userAppList == null){
                        userAppList = ToolUtils.getInstalledUserApps(context);
                    }
                    if (cacheList.size() == userAppList.size()){
                        //仪表盘中减分的子线程也结束
                        ybv.scanComplete();

                        //检测结束之后的操作
                        checked();
                    }
                    break;
            }
        }
    };

    /**
     * 检测结束之后的操作
     */
    private void checked(){
        //出现优化按钮
        bt_start.setText(R.string.majorization);
        bt_start.setClickable(true);
        bt_start.setEnabled(true);
        bt_start.setTextColor(Color.WHITE);

        //描述变为可清理的项数
        String major_des_g = context.getResources().getString(R.string.major_des_g);
        String major_des_b = context.getResources().getString(R.string.major_des_b);
        String des_unit = context.getResources().getString(R.string.des_unit);
        if (sumScroe >= 70) {
            tv_des.setText(major_des_g +" "+ 2 +" "+ des_unit);
        } else {
            tv_des.setText(major_des_b +" "+ 3 +""+ des_unit);
        }

    }


    //优化的异步handler
    private Handler handler = new Handler() {
        private String majoredCount;

        //插入的三个条目中的控件对象
        private LinearLayout ll_show1;
        private TextView item_count1;
        private LinearLayout ll_show2;
        private TextView item_count2;
        private LinearLayout ll_show3;
        private TextView item_count3;

        //每个阶段总得要展示的条目数和当前已经展示到的条目数【用于计算百分比】
        private int processTotalSize;
        private int processCurrentSize;
        private int cacheTotalSize;
        private int cacheCurrentSize;
        private int temTotalSize;
        private int temCurrentSize;

        //清理的垃圾大小和温度大小
        private long memTotal = 0;
        private long rubTotal = 0;
        private double temperature;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    processTotalSize = (int) msg.obj;
                    processCurrentSize = 0;

                    ll_parent.removeAllViews();

                    //插入第一个条目
                    View itemView1 = View.inflate(context, R.layout.fragment_item, null);
                    TextView item_des1 = (TextView) itemView1.findViewById(R.id.item_des);
                    item_count1 = (TextView) itemView1.findViewById(R.id.item_count);
                    ll_show1 = (LinearLayout) itemView1.findViewById(R.id.ll_show);

                    ll_parent.addView(itemView1, 0);
                    item_des1.setText(R.string.overmajorizate);
                    translateAnimation(itemView1);

                    memTotal = 0;

                    break;
                case 2:
                    ProcessInfo processInfo = (ProcessInfo) msg.obj;
                    //累加项数
                    processCurrentSize = processCurrentSize + 1;
                    //累加大小
                    memTotal = memTotal + processInfo.itemSize;

                    //进程集合的每一项累加的分数
                    int processSize = ToolUtils.getSize(processInfo.itemSize);  //转换为MB的大小
                    //根据系数计算增加分数
                    int processScore = (int) (processSize * ConstantValues.PROCESSPER);
                    //仪表盘增加
                    ybv.addScore(processScore);

                    //杀死这个进程
                    ActivityManager activityManger = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
                    activityManger.killBackgroundProcesses(processInfo.packageName);

                    //第一个条目的展示
                    TextView textView = new TextView(context);
                    textView.setText(processInfo.itemName);
                    textView.setTextSize(16);
                    textView.setTextColor(Color.GRAY);
                    textView.setPadding(0, 3, 0, 3);
                    ll_show1.addView(textView, 0);

                    String per1 = processCurrentSize * 100 / processTotalSize + "%";
                    item_count1.setText(per1);

                    break;
                case 3:
                    majoredCount = context.getResources().getString(R.string.MajoredCount);
                    tv_des.setText(majoredCount + 1);
                    cacheTotalSize = (int) msg.obj;
                    cacheCurrentSize = 0;

                    ll_parent.removeAllViews();

                    //插入第二个条目
                    View itemView2 = View.inflate(context, R.layout.fragment_item, null);
                    TextView item_des2 = (TextView) itemView2.findViewById(R.id.item_des);
                    item_count2 = (TextView) itemView2.findViewById(R.id.item_count);
                    ll_show2 = (LinearLayout) itemView2.findViewById(R.id.ll_show);

                    ll_parent.addView(itemView2, 0);
                    item_des2.setText(R.string.rubbishcleaned);
                    translateAnimation(itemView2);

                    rubTotal = 0;

                    break;
                case 4:
                    AppCacheInfo appCacheInfo = (AppCacheInfo) msg.obj;
                    //累加条目
                    cacheCurrentSize = cacheCurrentSize + 1;
                    //累加大小
                    rubTotal = rubTotal + appCacheInfo.cacheSize;

                    //每一个的大小
                    int cacheSize = ToolUtils.getSize(appCacheInfo.cacheSize);  //转换为MB的大小
                    //根据系数得到增加的分数
                    int cacheScore = (int) (cacheSize * ConstantValues.CACHEPER);
                    ybv.addScore(cacheScore);

                    //清除缓存
                    CleanAppCache.cleanAppCache(context,appCacheInfo.packageName);

                    //第二个条目的展示
                    TextView textView1 = new TextView(context);
                    textView1.setText(appCacheInfo.name);
                    textView1.setTextSize(16);
                    textView1.setTextColor(Color.GRAY);
                    textView1.setPadding(0, 3, 0, 3);
                    ll_show2.addView(textView1, 0);

                    String per2 = cacheCurrentSize * 100 / cacheTotalSize + "%";
                    item_count2.setText(per2);

                    break;

                case 5:
                    tv_des.setText(majoredCount + 2);
                    temTotalSize = (int) msg.obj;
                    temCurrentSize = 0;

                    ll_parent.removeAllViews();

                    //插入第三个条目
                    View itemView3 = View.inflate(context, R.layout.fragment_item, null);
                    TextView item_des3 = (TextView) itemView3.findViewById(R.id.item_des);
                    item_count3 = (TextView) itemView3.findViewById(R.id.item_count);
                    ll_show3 = (LinearLayout) itemView3.findViewById(R.id.ll_show);

                    ll_parent.addView(itemView3, 0);
                    item_des3.setText(R.string.temperature);
                    translateAnimation(itemView3);

                    break;

                case 6:
                    ProcessInfo temInfo = (ProcessInfo) msg.obj;
                    //累加条目
                    temCurrentSize = temCurrentSize + 1;

                    //第三个条目的展示
                    TextView textView2 = new TextView(context);
                    textView2.setText(temInfo.itemName);
                    textView2.setTextSize(16);
                    textView2.setTextColor(Color.GRAY);
                    textView2.setPadding(0, 3, 0, 3);
                    ll_show3.addView(textView2, 0);

                    String per3 = temCurrentSize * 100 / temTotalSize + "%";
                    item_count3.setText(per3);

                    break;

                case 7:
                    tv_des.setText(majoredCount + 3);

                    //需要结果展示界面
                    ll_main_ybv.setVisibility(View.GONE);
                    rl_main.setVisibility(View.GONE);

                    rl_result_fen.setVisibility(View.VISIBLE);
                    rl_result_item.setVisibility(View.VISIBLE);

                    //然后为结果界面初始化数据
                    BatteryDataInfo batteryInfo = BatteryDataInfo.getBatteryInfo();
                    temperature = batteryInfo.temperature;

                    fen.setText(String.valueOf(sumScroe));
                    tv_mem.setText(memTotal/ (1024 * 1024) + "MB");
                    tv_rub.setText(rubTotal/ (1024 * 1024) + "MB");
                    tv_tem.setText(temperature + "℃");

                    break;

            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parent = null;
        view = inflater.inflate(R.layout.main_fragment, parent);
        context = getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //初始化控件对象
        initUI();

        //初始进入应用的时候
            //按钮：灰色——正在检测
        bt_start.setText(R.string.checking);
        bt_start.setTextColor(0x66ffffff);
        bt_start.setClickable(false);
        bt_start.setEnabled(false);
            //描述：正在检测
        tv_des.setText(R.string.checking_des_f);

        //然后开始检测，获取得分数，让仪表盘分数递减
        getScroe();

        //设置所有按钮的监听事件
        setListener();

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 初始化控件对象
     */
    public void initUI() {
        ybv = (YiBiaoView) view.findViewById(R.id.ybv);       //自定义控件
        bt_start = (Button) view.findViewById(R.id.bt_start); //开始按钮
        tv_des = (TextView) view.findViewById(R.id.tv_des);   //检测描述

        //首页按钮的布局
        ll_button = (LinearLayout) view.findViewById(R.id.ll_button);
        bt_speed = (Button) view.findViewById(R.id.bt_speed);
        bt_battery = (Button) view.findViewById(R.id.bt_battery);
        bt_app = (Button) view.findViewById(R.id.bt_app);
        bt_virus = (Button) view.findViewById(R.id.bt_virus);
        bt_flow = (Button) view.findViewById(R.id.bt_flow);
        bt_more = (Button) view.findViewById(R.id.bt_more);

        //首页展示清理的布局
        ll_major = (LinearLayout) view.findViewById(R.id.ll_major);
        ll_parent = (LinearLayout) view.findViewById(R.id.ll_parent);
        bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

        //为了最后一个界面的结果展示工作
        //原界面上下两半部分
        ll_main_ybv = (LinearLayout) view.findViewById(R.id.ll_main_ybv);
        rl_main = (RelativeLayout) view.findViewById(R.id.rl_main);

        //新界面上下两半部分
        rl_result_fen = (RelativeLayout) view.findViewById(R.id.rl_result_fen);
        fen = (TextView) view.findViewById(R.id.fen);
        rl_result_item = (RelativeLayout) view.findViewById(R.id.rl_result_item);
        tv_mem = (TextView) view.findViewById(R.id.tv_mem);
        tv_rub = (TextView) view.findViewById(R.id.tv_rub);
        tv_tem = (TextView) view.findViewById(R.id.tv_tem);
        bt_over = (Button) view.findViewById(R.id.bt_over);
    }

    /**
     * 获取手机的评分
     */
    private void getScroe() {
        new Thread() {
            public void run() {
                //每次获取数据前先重新new集合
                processList = null;
                cacheList = null;
                processList = new ArrayList<>();
                cacheList = new ArrayList<>();

                //获取手机温度，得到评分，发送主线程扣减
                BatteryDataInfo batteryInfo = BatteryDataInfo.getBatteryInfo();
                double temperature = batteryInfo.temperature;

                if(temperature > 40 && isCheck){
                    //扣减的分数
                    Message msg1 = Message.obtain();
                    msg1.what = 1;
                    msg1.obj = (int) (temperature * ConstantValues.TEMPER);
                    mHandler.sendMessage(msg1);
                }


                //获取用户进程的集合
                ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                pm = context.getPackageManager();
                List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
                if (runningAppProcesses != null) {
                    for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                        if (isCheck){
                            ProcessInfo processInfo = new ProcessInfo();
                            //进程包名
                            processInfo.packageName = runningAppProcessInfo.processName;
                            //进程大小
                            android.os.Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});
                            android.os.Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
                            processInfo.itemSize = memoryInfo.getTotalPrivateDirty() * 1024;  //单位byte

                            try {
                                ApplicationInfo applicationInfo = pm.getApplicationInfo(processInfo.packageName, 0);
                                processInfo.itemName = applicationInfo.loadLabel(pm).toString();
                                processInfo.itemIcon = applicationInfo.loadIcon(pm);

                                //判断是否用户应用
                                processInfo.isSystem = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;

                            } catch (NameNotFoundException e) {
                                e.printStackTrace();
                                processInfo.itemName = processInfo.packageName;
                                processInfo.itemIcon = context.getResources().getDrawable(R.drawable.ic_launcher);
                            }

                            if (!processInfo.isSystem){
                                Message msg2 = Message.obtain();
                                msg2.what = 2;
                                msg2.obj = processInfo;
                                mHandler.sendMessage(msg2);

                                try {
                                    Thread.sleep(MAJOR_SLEEP);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                }


                //得到系统中的用户应用缓存垃圾
                //所有安装的用户应用
                userAppList = ToolUtils.getInstalledUserApps(context);
                for(PackageInfo packageInfo : userAppList){
                    if (isCheck){
                        String packageName = packageInfo.packageName; //包名
                        //通过反射获获取应用缓存大小
                        try{
                            Class<?> clazz = Class.forName("android.content.pm.PackageManager");
                            Method method = clazz.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                            method.invoke(pm, packageName, mStatsObserver);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(MAJOR_SLEEP);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //结束
            }
        }.start();
    }

    /**
     * 获取应用缓存的代码
     */
    final IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {
        private ApplicationInfo applicationInfo;
        //是运行在子线程中
        @Override
        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {

            long cacheSize = stats.cacheSize;   //缓存大小,单位为byte

            String packageName = stats.packageName;
            AppCacheInfo appCacheInfo = new AppCacheInfo();

            try {
                applicationInfo = pm.getApplicationInfo(packageName, 0);
                //名称
                appCacheInfo.name = applicationInfo.loadLabel(pm).toString();
                //图标
                appCacheInfo.icon = applicationInfo.loadIcon(pm);
                //包名
                appCacheInfo.packageName = packageName;
                //大小
                appCacheInfo.cacheSize = cacheSize;

            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            Message msg3 = Message.obtain();
            msg3.what = 3;
            msg3.obj = appCacheInfo;
            mHandler.sendMessage(msg3);
        }
    };


    /**
     * 按钮点击的监听
     */
    public void setListener(){
        //点击优化按钮
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_start = bt_start.getText().toString();
                String majorization = context.getResources().getString(R.string.majorization);
                String recheck = context.getResources().getString(R.string.recheck);
                if (str_start.equals(majorization)){  //点击优化
                    //打开子线程的开关
                    isMajor = true;

                    //按钮文字为优化中，字体变灰色
                    bt_start.setText(R.string.majoring);
                    bt_start.setTextColor(0x66ffffff);
                    bt_start.setClickable(false);
                    bt_start.setEnabled(false);

                    //描述变为已经完成优化的项数，并且动画
                    tv_des.setText(R.string.MajoredCount);

                    ObjectAnimator tranY_des = ObjectAnimator.ofFloat(tv_des, "translationY", 0, 100);
                    tranY_des.setDuration(200);
                    tranY_des.setRepeatCount(1);
                    tranY_des.setRepeatMode(ObjectAnimator.REVERSE);
                    tranY_des.start(); //执行动画

                    //ll_button消失
                    ObjectAnimator tranY_button = ObjectAnimator.ofFloat(ll_button, "translationY", 0, 800);
                    tranY_button.setDuration(200);
                    tranY_button.setRepeatCount(0);
                    tranY_button.start(); //执行动画

                    //ll_major出现
                    ObjectAnimator tranY_major = ObjectAnimator.ofFloat(ll_major, "translationY", 0, 800);
                    tranY_major.setDuration(200);
                    tranY_major.setRepeatCount(1);
                    tranY_major.setRepeatMode(ObjectAnimator.REVERSE);
                    tranY_major.start(); //执行动画


                    //开始优化的异步过程
                    startClean();

                }else if (str_start.equals(recheck)){   //重新检测
                    //重新开始检测的业务逻辑
                    //按钮：灰色——正在检测
                    bt_start.setText(R.string.checking);
                    bt_start.setTextColor(0x66ffffff);
                    bt_start.setClickable(false);
                    bt_start.setEnabled(false);
                    //描述：正在检测
                    tv_des.setText(R.string.checking_des_s);

                    //然后开始检测，获得分数，让仪表盘分数递减
                    getScroe();
                }


            }
        });

        //取消优化按钮
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            private int mm;
            @Override
            public void onClick(View v) {
                //优化的进程结束
                isMajor = false;

                //按钮变为重新检测
                bt_start.setText(R.string.recheck);
                bt_start.setClickable(true);
                bt_start.setEnabled(true);
                bt_start.setTextColor(Color.WHITE);

                //描述变为上次检测已经取消，请重新检测
                tv_des.setText(R.string.checking_des_t);
                ObjectAnimator tranY_des = ObjectAnimator.ofFloat(tv_des, "translationY", 0, 100);
                tranY_des.setDuration(200);
                tranY_des.setRepeatCount(1);
                tranY_des.setRepeatMode(ObjectAnimator.REVERSE);
                tranY_des.start(); //执行动画

                //ll_button出现
                ObjectAnimator tranY_button = ObjectAnimator.ofFloat(ll_button, "translationY", 0, 800);
                tranY_button.setDuration(200);
                tranY_button.setRepeatCount(1);
                tranY_button.setRepeatMode(ObjectAnimator.REVERSE);
                tranY_button.start(); //执行动画

                //ll_major消失
                ObjectAnimator tranY_major = ObjectAnimator.ofFloat(ll_major, "translationY", 0, 800);
                tranY_major.setDuration(200);
                tranY_major.setRepeatCount(0);
                tranY_major.start(); //执行动画

                //仪表盘归零
                ybv.addScore(100);

            }
        });


        //完成按钮【结果展示界面】
        bt_over.setOnClickListener(new View.OnClickListener() {
            private int aa;
            @Override
            public void onClick(View v) {
                //回到主界面
                rl_result_fen.setVisibility(View.GONE);
                rl_result_item.setVisibility(View.GONE);
                rl_result_item.setClickable(false);
                rl_result_item.setEnabled(false);
                rl_result_item.setFocusable(false);

                rl_main.setVisibility(View.VISIBLE);
                ll_main_ybv.setVisibility(View.VISIBLE);

                //按钮变为重新检测
                bt_start.setText(R.string.recheck);
                bt_start.setClickable(true);
                bt_start.setEnabled(true);
                bt_start.setTextColor(Color.WHITE);

                //UI界面的更改，反方向的动画，恢复优化前的样子，并且动画
                //描述变为上次优化时间
                String hasMajored = context.getResources().getString(R.string.hasMajored);
                SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm");
                String date = sDateFormat.format(new java.util.Date());
                tv_des.setText(hasMajored + date);
                ObjectAnimator tranY_des = ObjectAnimator.ofFloat(tv_des, "translationY", 0, 100);
                tranY_des.setDuration(200);
                tranY_des.setRepeatCount(1);
                tranY_des.setRepeatMode(ObjectAnimator.REVERSE);
                tranY_des.start(); //执行动画

                //ll_button出现
                ObjectAnimator tranY_button = ObjectAnimator.ofFloat(ll_button, "translationY", 0, 800);
                tranY_button.setDuration(200);
                tranY_button.setRepeatCount(1);
                tranY_button.setRepeatMode(ObjectAnimator.REVERSE);
                tranY_button.start(); //执行动画

                //ll_major消失
                ObjectAnimator tranY_major = ObjectAnimator.ofFloat(ll_major, "translationY", 0, 800);
                tranY_major.setDuration(200);
                tranY_major.setRepeatCount(0);
                tranY_major.start(); //执行动画

            }
        });

        //按钮的点击事件
        bt_speed.setOnClickListener(this);
        bt_battery.setOnClickListener(this);
        bt_app.setOnClickListener(this);
        bt_virus.setOnClickListener(this);
        bt_flow.setOnClickListener(this);
        bt_more.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        switch (v.getId()) {
            case R.id.bt_speed:
                //回退栈管理
                tx.hide(this);
                tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                tx.add(R.id.main_content, new CleanerFragment(), "CLEAN");
                tx.addToBackStack(null);
                tx.commit();
                break;

            case R.id.bt_battery:
                tx.hide(this);
                tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                tx.add(R.id.main_content, new BatteryFragment(), "BATTERY");
                tx.addToBackStack(null);
                tx.commit();
                break;

            case R.id.bt_app:
                tx.hide(this);
                tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                tx.add(R.id.main_content, new AppFragment(), "APP");
                tx.addToBackStack(null);
                tx.commit();
                break;

            case R.id.bt_virus:
                tx.hide(this);
                tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                tx.add(R.id.main_content, new AntiVirusFragment(), "ANTIVIRUS");
                tx.addToBackStack(null);
                tx.commit();
                break;

            case R.id.bt_flow:
                tx.hide(this);
                tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                tx.add(R.id.main_content, new MonitorFragment(), "MONITOR");
                tx.addToBackStack(null);
                tx.commit();
                break;

            case R.id.bt_more:
                //更多
                tx.hide(this);
                tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                tx.add(R.id.main_content, new MoreFragment(), "MORE");
                tx.addToBackStack(null);
                tx.commit();

                break;
        }
    }

    /**
     * 开始优化的子线程
     */
    public void startClean() {
        new Thread() {
            @Override
            public void run(){
                //先等动画执行完毕：
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (isMajor) {
                    //然后通知插入第一个条目
                    Message msg1 = Message.obtain();
                    msg1.what = 1;
                    msg1.obj = processList.size();
                    handler.sendMessage(msg1);

                    try {
                        Thread.sleep(300); //等条目插入完成
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (isMajor){
                    //第一个条目的展示
                    for (ProcessInfo processInfo : processList) {
                        if (isMajor) {
                            Message msg2 = Message.obtain();
                            msg2.what = 2;
                            msg2.obj = processInfo;
                            handler.sendMessage(msg2);

                            try {
                                Thread.sleep(CLEAN_SLEEP);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else{
                            break;
                        }
                    }
                }

                if (isMajor) {
                    //通知插入第二个条目
                    Message msg3 = Message.obtain();
                    msg3.what = 3;
                    msg3.obj = cacheList.size();
                    handler.sendMessage(msg3);

                    try {
                        Thread.sleep(300);  //等条目插入完成
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (isMajor){
                    //第二个条目的展示
                    for (AppCacheInfo appCacheInfo : cacheList) {
                        if (isMajor) {
                            Message msg4 = Message.obtain();
                            msg4.what = 4;
                            msg4.obj = appCacheInfo;
                            handler.sendMessage(msg4);

                            try {
                                Thread.sleep(CLEAN_SLEEP);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else{
                            break;
                        }
                    }
                }

                //准备一个集合，用于温度条目的展示，暂时还用进程集合吧
                ArrayList<ProcessInfo> temperatureList = processList;
                if (isMajor) {
                    //通知插入第三个条目
                    Message msg5 = Message.obtain();
                    msg5.what = 5;
                    msg5.obj = temperatureList.size();
                    handler.sendMessage(msg5);

                    try {
                        Thread.sleep(300);  //等条目插入完成
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (isMajor){
                    //第三个条目的展示
                    for (ProcessInfo processInfo : temperatureList) {
                        if (isMajor){
                            Message msg6 = Message.obtain();
                            msg6.what = 6;
                            msg6.obj = processInfo;
                            handler.sendMessage(msg6);

                            try {
                                Thread.sleep(CLEAN_SLEEP);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                if (isMajor) {
                    //最后完成所有的检测工作
                    Message msg7 = Message.obtain();
                    msg7.what = 7;
                    handler.sendMessage(msg7);
                }
            }
        }.start();
    }

    /**
     * 清理条目平移插入的动画
     * @param view 要做动画的view对象
     */
    public void translateAnimation(View view) {
        TranslateAnimation ta =
                new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0);
        ta.setDuration(300);
        ta.setFillAfter(true);
        view.startAnimation(ta);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UMengStaticValue.MAIN_FGM);
    }

    @Override
    public void onStop() {
        super.onStop();
        MobclickAgent.onPageEnd(UMengStaticValue.MAIN_FGM);
    }


}