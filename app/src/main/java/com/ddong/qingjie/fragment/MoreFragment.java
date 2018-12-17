package com.ddong.qingjie.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ddong.qingjie.R;
import com.ddong.qingjie.notification.NoticeService;

/**
 * Created by Antony on 2016/1/20.
 */
public class MoreFragment extends Fragment {


    private Context context;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        context = getActivity();
        view = View.inflate(context, R.layout.more_fragment, null);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        //回退按钮
        Button bt_more = (Button) view.findViewById(R.id.bt_more);
        bt_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                while (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate();
                }

            }
        });


        //去评分条目
        RelativeLayout rl_google = (RelativeLayout) view.findViewById(R.id.rl_google);
        rl_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.catchfigt.trashclear"));
                    intent.setPackage("com.android.vending");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException anfe) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.ddong.qingjie"));
                    startActivity(intent);
                }
            }
        });

        //开始通知栏的条目
        startNotice();

        super.onActivityCreated(savedInstanceState);
    }


    /**
     * 通知栏条目的开关
     */
    private ImageView iv_notice_switch;
    private SharedPreferences sp;
    private boolean isNotice;
    public void startNotice(){
        RelativeLayout rl_notice = (RelativeLayout) view.findViewById(R.id.rl_notice);
        iv_notice_switch = (ImageView) view.findViewById(R.id.iv_notice_switch);
        //获取sp对象
        sp = context.getSharedPreferences("Notice", Context.MODE_PRIVATE);
        isNotice = sp.getBoolean("IsNotice", true);
        //初始化时，根据sp保存的true和false设置图标
        if (isNotice) {
            iv_notice_switch.setImageResource(R.drawable.switch_open);
        } else {
            iv_notice_switch.setImageResource(R.drawable.switch_close);
        }
        //再设置点击开关之后的事件
        rl_notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击之后，isNotice变为相反
                isNotice = !isNotice;
                //再根据true和false设置图标
                if (isNotice) {
                    iv_notice_switch.setImageResource(R.drawable.switch_open);
                    //开启服务
                    Intent intent = new Intent(context, NoticeService.class);
                    context.startService(intent);

                } else {
                    iv_notice_switch.setImageResource(R.drawable.switch_close);
                    //关闭服务
                    Intent intent = new Intent(context, NoticeService.class);
                    context.stopService(intent);
                }

                //保存点击之后的结果
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("IsNotice", isNotice);
                edit.commit();

            }
        });


    }



}
