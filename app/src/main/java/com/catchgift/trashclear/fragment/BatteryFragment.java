package com.catchgift.trashclear.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.catchgift.trashclear.R;
import com.catchgift.trashclear.batteryfragment.BrightnessTools;
import com.catchgift.trashclear.batteryfragment.activity.BatteryRanklistActivity;
import com.catchgift.trashclear.sys.CleanMemory;
import com.catchgift.trashclear.utils.UMengStaticValue;
import com.catchgift.trashclear.utils.YLog;
import com.catchgift.trashclear.notification.NoticeActivity;
import com.catchgift.trashclear.utils.SaveUtils;
import com.umeng.analytics.MobclickAgent;

public class BatteryFragment extends Fragment {

    private View view;
    private Context context;
    private Button battery_back;

    private TextView tv_power_left;
    private TextView tv_wait_time;
    private RelativeLayout rl_power_rank;

    private TextView tv_tem;
    private TextView tv_3G;
    private TextView tv_wifi;

    private RelativeLayout rl_battery_sleep;
    private ImageView iv_sleep_switch;
    private Button bt_power_saving;

    //电量100%时的待机时间[单位小时]
    private int totalWait = 24;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        view = inflater.inflate(R.layout.battery_fragment, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //初始化控件对象
        initUI();

        //设置界面数据
        initData();

        //设置锁频休眠条目
        setComplexItem();

        //设置一键省电按钮
        setSavingPowerItem();

        super.onActivityCreated(savedInstanceState);
    }

    private void initUI() {
        battery_back = (Button) view.findViewById(R.id.battery_back);

        tv_power_left = (TextView) view.findViewById(R.id.tv_power_left);
        tv_wait_time = (TextView) view.findViewById(R.id.tv_wait_time);
        rl_power_rank = (RelativeLayout) view.findViewById(R.id.rl_power_rank);


        tv_tem = (TextView) view.findViewById(R.id.tv_tem);
        tv_3G = (TextView) view.findViewById(R.id.tv_3G);
        tv_wifi = (TextView) view.findViewById(R.id.tv_wifi);

        rl_battery_sleep = (RelativeLayout) view.findViewById(R.id.rl_battery_sleep);
        iv_sleep_switch = (ImageView) view.findViewById(R.id.iv_sleep_switch);
        bt_power_saving = (Button) view.findViewById(R.id.bt_power_saving);

        //如果从通知栏跳转，实现一键省电
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.getBackStackEntryCount() == 0){
            bt_power_saving.performClick();
        }
    }

    public void initData() {
        //返回按钮的点击事件
        battery_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fragmentManager = getFragmentManager();
                if(fragmentManager.getBackStackEntryCount() > 0){
                    fragmentManager.popBackStackImmediate();
                }else{
                    //从通知栏进入的关闭方式
                    FragmentActivity activity = getActivity();
                    if (activity instanceof NoticeActivity){
                        NoticeActivity noticeActivity = (NoticeActivity) activity;
                        noticeActivity.finish();
                    }
                }
            }
        });

//        //获取电量和温度
//        BatteryDataInfo batteryInfo = BatteryDataInfo.getBatteryInfo();
//        int current = batteryInfo.current;  //当前电量
//        int total = batteryInfo.total;      //总电量
//        double temperature = batteryInfo.temperature;   //温度

        //温度、电量
        SharedPreferences sp = context.getSharedPreferences("NoticeTem", Context.MODE_PRIVATE);
        double temperature = sp.getInt("tem",0);
        int current = sp.getInt("curr",0);
        int total = 100;


        //设置剩余电量百分比
        int mCurrentPre = current * 100 / total;
        tv_power_left.setText(String.valueOf(mCurrentPre));


        //设置可待机时间
        String hour = context.getResources().getString(R.string.hour);
        String minute = context.getResources().getString(R.string.minute);
        String str_wait_time_tag = context.getResources().getString(R.string.standby);
        //可待机
        SpannableStringBuilder word1 = new SpannableStringBuilder();
        word1.append(str_wait_time_tag);
        word1.setSpan(new RelativeSizeSpan(1), 0, str_wait_time_tag.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        //时间
        float waitTime = (float) (totalWait*mCurrentPre*1.0/100);
        float[] time = mathabs(waitTime);
        //拼接字体不同大小的字符串
        tv_wait_time.setText(word1.append(getResult(String.valueOf((int) time[0]), hour, String.valueOf((int) time[1]), minute)));

        //耗电排行点击事件
        MobclickAgent.onEvent(context, UMengStaticValue.CLICK_KILLPROCESS_BTN);
        rl_power_rank.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BatteryRanklistActivity.class);
                startActivity(intent);
            }
        });

        //设置手机温度，3G通话和wifi上网时间
        String str_tem = (int)(temperature+0.5) + "℃";
        SpannableStringBuilder word = new SpannableStringBuilder();
        word.append(str_tem);
        word.setSpan(new RelativeSizeSpan(2), 0, str_tem.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        tv_tem.setText(word);

        //计算三个可用时间（单位小时）
        float callTime = current / 17f;
        float wifiTime = current / 14f;
        //时间转换
        float[] cTime = mathabs(callTime);
        float[] wTime = mathabs(wifiTime);
        //设置拼接后的字符串
        tv_3G.setText(getResult(String.valueOf((int)cTime[0]),hour,String.valueOf((int)cTime[1]),minute));
        tv_wifi.setText(getResult(String.valueOf((int)wTime[0]),hour,String.valueOf((int)wTime[1]),minute));

    }


    /**
     * 拼接可用时间字符串
     */
    private SpannableStringBuilder getResult(String one,String two,String three,String four){
        int start;
        int end;
        SpannableStringBuilder word = new SpannableStringBuilder();
        word.append(one);
        start = 0;
        end = one.length();
        word.setSpan(new RelativeSizeSpan(2), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        word.append(two);
        start = end;
        end += two.length();
        word.setSpan(new RelativeSizeSpan(1), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        word.append(three);
        start = end;
        end += three.length();
        word.setSpan(new RelativeSizeSpan(2), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        word.append(four);
        start = end;
        end += four.length();
        word.setSpan(new RelativeSizeSpan(1), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return word;
    }


    /**
     * 设置锁屏休眠条目
     */
    private SharedPreferences sp;
    private boolean isStart;
    private void setComplexItem() {
        //获取sp对象
        sp = context.getSharedPreferences("YeahTools", Context.MODE_PRIVATE);
        isStart = sp.getBoolean("IsStart", false);
        //初始进入时，根据true和false设置图标
        if (isStart) {
            iv_sleep_switch.setImageResource(R.drawable.switch_open);
        } else {
            iv_sleep_switch.setImageResource(R.drawable.switch_close);
        }

        //点击锁频休眠条目
        MobclickAgent.onEvent(context, UMengStaticValue.CLICK_KILLPROCESS_BTN);
        rl_battery_sleep.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = !isStart;
                //点击之后，根据true和false设置图标
                if (isStart) {
                    iv_sleep_switch.setImageResource(R.drawable.switch_open);
                } else {
                    iv_sleep_switch.setImageResource(R.drawable.switch_close);
                }

                //保存点击之后的结果
                Editor edit = sp.edit();
                edit.putBoolean("IsStart", isStart);
                edit.commit();

                //将是否开启的结果存储到私有目录的文件中
                SaveUtils.saveUserInfo(context, isStart);
            }
        });
    }

    /**
     * 一键省电按钮的点击事件
     */
    private void setSavingPowerItem(){
        MobclickAgent.onEvent(context, UMengStaticValue.CLICK_KILLPROCESS_BTN);
        bt_power_saving.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //异步的方法杀死进程
                CleanMemory cleanMemory = new CleanMemory(new CleanMemory.Listener() {
                    @Override
                    public void OnCleanListener(String pid, String pkg, String name, String ver) {
                    }

                    @Override
                    public void OnCleanCompleteListener(String cleanMem) {
                    }
                }, getActivity());
                cleanMemory.startClean();

                //获取是否开启亮度自动调节
                boolean autoBrightness = BrightnessTools.isAutoBrightness(getActivity().getContentResolver());
                if (autoBrightness) {
                    //关闭自动调节
                    BrightnessTools.stopAutoBrightness(getActivity());
                }
                //获取当前屏幕亮度
                int screenBrightness = BrightnessTools.getScreenBrightness(getActivity());
                if (screenBrightness > 60) {
                    //设置屏幕亮度
                    BrightnessTools.setBrightness(getActivity(), 50);
                    //保存设置到系统
                    BrightnessTools.saveBrightness(getActivity().getContentResolver(), 50);
                }

                bt_power_saving.setText(R.string.alreadysave);
                bt_power_saving.setTextColor(context.getResources().getColor(R.color.ban_gray));
                bt_power_saving.setEnabled(false);
            }
        });

    }


    /**
     * 计算当前电量可以换算成的时间,用数组存放
     */
    private float[] mathabs(float num) {
        float[] n = new float[2];
        int numTime = (int) Math.abs(num);
        n[0] = numTime;
        float decimals = num - numTime;
        YLog.i("decTime", decimals + "");
        if (decimals == 0) {
            return n;
        } else {
            float decTime = decimals * 60;
            n[1] = decTime;
            return n;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UMengStaticValue.BATT_FGM);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UMengStaticValue.BATT_FGM);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
