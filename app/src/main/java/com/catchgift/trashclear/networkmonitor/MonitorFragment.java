package com.catchgift.trashclear.networkmonitor;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.catchgift.trashclear.R;
import com.catchgift.trashclear.utils.ToastUtils;
import com.catchgift.trashclear.utils.ToolUtils;
import com.catchgift.trashclear.notification.NoticeActivity;

import java.util.HashMap;

/**
 * Created by TommyDuan on 16/1/6 at 上午9:55 in Deepclean.
 */
public class MonitorFragment extends Fragment implements View.OnClickListener{

    private View view;
    private Context mContext;
    private Button monitor_back;
    private TextView monitor_num;
    private TextView monitor_least;
    private TextView tv_des1;
    private TextView tv_des2;
    private TextView tv_des3;
    private RelativeLayout monitor_rank;
    private Button bt_monitor;
    //是否开启数据流量
    private Boolean isMobileType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.monitor_fragment, null);
        mContext = getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initUI();

        //获取是否开启手机卡流量
        isMobileType = ToolUtils.getNetworkInfo(mContext);
        if (isMobileType){  //开启
            //初始化设置数据
            initData();
        }else{              //关闭
            String str1 = mContext.getResources().getString(R.string.toast1);
            ToastUtils.show(mContext, str1);
        }

        setListener();
        super.onActivityCreated(savedInstanceState);
    }

    private void initData() {
        HashMap<String, Object> data = MonitorUtils.getCurrentMobiUsed();
        String usedflow = mContext.getResources().getString(R.string.usedflow);
        String leftday = mContext.getResources().getString(R.string.leftday);
        String unitday = mContext.getResources().getString(R.string.day);
        if (data == null){
            //还未设置数据
            String str2 = mContext.getResources().getString(R.string.toast3);
            ToastUtils.show(mContext,str2);
            monitor_num.setText(String.valueOf(0));  //剩余流量
            tv_des2.setText(usedflow + String.valueOf(0) + "MB");  //已经使用流量
            tv_des3.setText(leftday + String.valueOf(0) + unitday);   //剩余填出

        }else {
            //已经设置数据
            long rx = (Long) data.get("rx"); //接收字节
            long tx = (Long) data.get("tx"); //发送字节
            long li = (Long) data.get("li"); //流量限制，即总流量
            long le = (Long) data.get("le"); //设置时间长度
            float day = (Float) data.get("dy"); //剩余日期

            //已经使用
            long total = rx + tx;
            int tt = (int) (total/(1024*1024));
            tv_des2.setText(usedflow + tt + unitday);

            //剩余流量
            int left = (int) ((li - total)/(1024*1024));
            monitor_num.setText(String.valueOf(left));

            //剩余天数
            int time = (int) day;
            if (time < day){
                time = time + 1;
            }

            tv_des3.setText(leftday+ time + unitday);
        }
    }

    public void initUI(){
        monitor_back = (Button)view.findViewById(R.id.monitor_back);
        monitor_num = (TextView)view.findViewById(R.id.monitor_num);
        monitor_least = (TextView)view.findViewById(R.id.monitor_least);
        monitor_least.setText(R.string.flow_least);
        tv_des1 = (TextView)view.findViewById(R.id.tv_des1);
        tv_des2 = (TextView)view.findViewById(R.id.tv_des2);
        tv_des3 = (TextView)view.findViewById(R.id.tv_des3);

        monitor_rank = (RelativeLayout)view.findViewById(R.id.monitor_rank);
        bt_monitor = (Button)view.findViewById(R.id.bt_monitor);
    }


    public void setListener(){
        //返回按钮
        monitor_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                if(fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate();
                }else{
                    //通知栏进入的关闭方式
                    FragmentActivity activity = getActivity();
                    if (activity instanceof NoticeActivity){
                        NoticeActivity noticeActivity = (NoticeActivity) activity;
                        noticeActivity.finish();
                    }
                }
            }
        });

        //流量排行
        monitor_rank.setOnClickListener(this);

        //流量设置
        bt_monitor.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        if (fm.getBackStackEntryCount() > 0){   //正常进入
            switch (v.getId()) {
                //点击流量排行条目
                case R.id.monitor_rank:
                    //回退栈管理
                    tx.hide(this);
                    tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    tx.add(R.id.main_content, new FlowRankFragment(), "FLOWRANK");
                    tx.addToBackStack(null);
                    tx.commit();
                    break;

                //点击流量设置按钮
                case R.id.bt_monitor:
                    //每次点击按钮的时候先获取是否开启才靠谱
                    isMobileType = ToolUtils.getNetworkInfo(mContext);
                    //判断是否开启，然后决定按钮的点击事件
                    if (isMobileType){
                        //开启，打开新fragment
                        tx.remove(this);
                        tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        tx.replace(R.id.main_content, new PlanSettingFragment(), "SETTING");
                        tx.addToBackStack(null);
                        tx.commit();

                    }else{
                        //弹出一个对话框，提示是否进行流量开关的设置
                        ToolUtils.setNetworkMethod(mContext);
                    }

                    break;
            }

        }else{                                  //通知栏进入
            switch (v.getId()) {
                //点击流量排行条目
                case R.id.monitor_rank:
                    //回退栈管理
                    tx.hide(this);
                    tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    tx.add(R.id.notice_layout, new FlowRankFragment(), "FLOWRANK");
                    tx.addToBackStack(null);
                    tx.commit();
                    break;

                //点击流量设置按钮
                case R.id.bt_monitor:
                    //每次点击按钮的时候先获取是否开启才靠谱
                    isMobileType = ToolUtils.getNetworkInfo(mContext);
                    //判断是否开启，然后决定按钮的点击事件
                    if (isMobileType){
                        //开启，打开新fragment
                        tx.remove(this);
                        tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        tx.replace(R.id.notice_layout, new PlanSettingFragment(), "SETTING");
                        tx.addToBackStack(null);
                        tx.commit();

                    }else{
                        //弹出一个对话框，提示是否进行流量开关的设置
                        ToolUtils.setNetworkMethod(mContext);
                    }

                    break;
            }


        }

    }


}