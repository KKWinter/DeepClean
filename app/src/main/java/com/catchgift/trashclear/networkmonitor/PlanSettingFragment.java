package com.catchgift.trashclear.networkmonitor;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.catchgift.trashclear.R;
import com.catchgift.trashclear.utils.ToastUtils;
import com.catchgift.trashclear.utils.ToolUtils;

import java.util.HashMap;

/**
 * Created by Antony on 2016/1/11.
 */
public class PlanSettingFragment extends Fragment{

    private Context context;
    private View view;
    private Button bt_setting;

    private LinearLayout ll_unit;
    private EditText et_usable_size;
    private TextView tv_unit;

    private RelativeLayout rl_outofdate;
    private TextView tv_least;
    private Button save;
    private Button cancel;

    // 声明unitPopupWindow对象的引用
    private PopupWindow popupWindow;
    private View unit_view;
    //声明api11以上版本的datePopWindow对象的引用
    private PopupWindow datePopWindow;
    private View date_view;
    //声明api11以下版本的使用
    private PopupWindow lowDatePopWindow;
    private View low_date_view;

    //是否开启手机卡流量
    private boolean isMobileType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        view = View.inflate(context, R.layout.plan_setting_fragment, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        initUI();
        setListener();

        isMobileType = ToolUtils.getNetworkInfo(context);
        if (isMobileType){
            String toast2 = context.getResources().getString(R.string.toast2);
            ToastUtils.show(context,toast2);

            //初始化设置数据
            initData();
        }


        super.onActivityCreated(savedInstanceState);
    }

    private void initData() {
        HashMap<String, Object> data = MonitorUtils.getCurrentMobiUsed();
        if (data == null){
            //为空，未设置数据
            String toast3 = context.getResources().getString(R.string.toast3);
            ToastUtils.show(context,toast3);
        }else {
            long rx = (Long) data.get("rx"); //接收字节
            long tx = (Long) data.get("tx"); //发送字节
            long li = (Long) data.get("li"); //流量限制
            long le = (Long) data.get("le"); //设置时间长度
            float day = (Float) data.get("dy"); //剩余日期

            //已经使用的流量
            long total = rx + tx;
            //剩余可用流量【流量上限】
            int left = (int) ((li - total)/(1024*1024));
            et_usable_size.setText(String.valueOf(left));

            //剩余天数
            int num = (int) day;
            if (num < day){
                num = num + 1;
            }
            String lefttime = context.getResources().getString(R.string.let);
            String leftday = context.getResources().getString(R.string.day);
            tv_least.setText(lefttime +" "+num +" "+ leftday);

        }
    }

    private void initUI(){
        //返回按钮
        bt_setting = (Button) view.findViewById(R.id.bt_setting);
        //第一个条目
        et_usable_size = (EditText) view.findViewById(R.id.et_usable_size);
        ll_unit = (LinearLayout) view.findViewById(R.id.ll_unit);
        tv_unit = (TextView) view.findViewById(R.id.tv_unit);
        //第二个条目
        rl_outofdate = (RelativeLayout) view.findViewById(R.id.rl_outofdate);
        tv_least = (TextView) view.findViewById(R.id.tv_least);
        //确定按钮
        save = (Button) view.findViewById(R.id.save);
        cancel = (Button) view.findViewById(R.id.cancel);
    }

    public void setListener(){
        //返回按钮
        bt_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                if(fragmentManager.getBackStackEntryCount() > 1) {
                    fragmentManager.popBackStackImmediate();
                }else{
                    //通知栏进入的退出方式
                    fragmentManager.popBackStackImmediate();
                }
            }
        });


        //选择可用流量单位
        ll_unit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //底部弹出一个popWindow
                showUnitPopWindow();
                //这里是位置显示方式,在屏幕的下方
                popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                //设置弹出popWindow的时候，背景为透明
                backgroundAlpha(0.6f);
            }
        });


        //剩余天数条目的点击事件
        rl_outofdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断版本是否大于api11
                if(android.os.Build.VERSION.SDK_INT > 10){

                    //底部弹出一个popWindow
                    showDatePopWindow();
                    //这里是位置显示方式,在屏幕的下方
                    datePopWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                    //设置弹出popWindow的时候，背景为透明
                    backgroundAlpha(0.6f);
                }else{

                    //底部弹出一个popWindow
                    showLowDatePopWindow();
                    //这里是位置显示方式,在屏幕的下方
                    lowDatePopWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                    //设置弹出popWindow的时候，背景为透明
                    backgroundAlpha(0.6f);

                }
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMobileType) {
                    //点击保存的时候，如果设置为空，提示不能保存
                    if (et_usable_size.getText().toString().equals("")) {
                        String flowtop = context.getResources().getString(R.string.flowtop);
                        ToastUtils.show(context, flowtop);

                    } else {
                        Long data;
                        if (tv_unit.getText().equals("MB")) {
                            data = Long.parseLong(et_usable_size.getText().toString()) * 1024 * 1024;
                        } else {
                            data = Long.parseLong(et_usable_size.getText().toString()) * 1024 * 1024 * 1024;
                        }
                        //取出数据
                        String str = tv_least.getText().toString();
                        int time = ToolUtils.getNum(str);

                        MonitorUtils.setLimit(data, time);

                        bt_setting.performClick();
                    }

                }


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击取消退出
                bt_setting.performClick();
            }
        });
    }

    /**
     * 创建unitPopupWindow
     */
    protected void showUnitPopWindow() {
        //初始化popwindow布局
        initView();

        //创建PopupWindow实例,LayoutParams.MATCH_PARENT和170分别是宽度和高度，true表示可以接收点击事件
        popupWindow = new PopupWindow(unit_view, ViewGroup.LayoutParams.MATCH_PARENT, dp2px(85), true);
        // 设置弹出的动画效果
        popupWindow.setAnimationStyle(R.style.AnimationFade);
        //设置背景之后，点击返回键或屏幕其他地方popwindow才会关闭
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //监听popWindow的dimiss事件
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);//popWindow关闭之后恢复背景透明度
            }
        });
    }

    public void initView(){
        // 获取自定义布局文件的视图
        unit_view = View.inflate(context, R.layout.plansetting_unit_view, null);
        TextView tv_mb = (TextView) unit_view.findViewById(R.id.tv_mb);
        TextView tv_gb = (TextView) unit_view.findViewById(R.id.tv_gb);

        tv_mb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_unit.setText("MB");
                popupWindow.dismiss();
                popupWindow = null;
                backgroundAlpha(1f); //恢复背景的透明度
            }
        });

        tv_gb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_unit.setText("GB");
                popupWindow.dismiss();
                popupWindow = null;
                backgroundAlpha(1f);
            }
        });
    }

    /**
     * 创建datePopWindow
     */
    protected  void showDatePopWindow(){
        //初始化popwindow布局
        initDateView();

        //创建popDateWindow实例
        datePopWindow = new PopupWindow(date_view, ViewGroup.LayoutParams.MATCH_PARENT, dp2px(280), true);
        // 设置弹出的动画效果
        datePopWindow.setAnimationStyle(R.style.AnimationFade);
        //设置背景之后，点击返回键或屏幕其他地方popwindow才会关闭
        datePopWindow.setBackgroundDrawable(new BitmapDrawable());
        //防止虚拟软键盘被弹出菜单遮住
        datePopWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //监听popWindow的dimiss事件
        datePopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);//popWindow关闭之后恢复背景透明度

            }
        });
    }

    //选择的数字
    private int np_time;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initDateView(){
        date_view = View.inflate(context, R.layout.plansetting_date_view, null);
        Button date_cancel = (Button) date_view.findViewById(R.id.date_cancel);
        Button date_confirm = (Button) date_view.findViewById(R.id.date_confirm);
        NumberPicker np = (NumberPicker) date_view.findViewById(R.id.np);
        np.setMinValue(0);
        np.setMaxValue(30);
        //获取当前的时间
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            //当前选中的时间
            np_time = newVal;
            }

        });

        date_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePopWindow.dismiss();
                datePopWindow = null;
                backgroundAlpha(1f);
            }
        });

        date_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String least = context.getResources().getString(R.string.least);
                String day = context.getResources().getString(R.string.day);
                tv_least.setText(least + " " + np_time + " "+ day);
                datePopWindow.dismiss();
                datePopWindow = null;
                backgroundAlpha(1f);
            }
        });

    }


    /**
     * API11以下的版本，显示输入框
     */
    protected  void showLowDatePopWindow(){
        //初始化popwindow布局
        initLowDateView();

        //创建popDateWindow实例
        lowDatePopWindow = new PopupWindow(low_date_view, ViewGroup.LayoutParams.MATCH_PARENT, dp2px(200), true);
        // 设置弹出的动画效果
        lowDatePopWindow.setAnimationStyle(R.style.AnimationFade);
        //设置背景之后，点击返回键或屏幕其他地方popwindow才会关闭
        lowDatePopWindow.setBackgroundDrawable(new BitmapDrawable());
        //防止虚拟软键盘被弹出菜单遮住
        lowDatePopWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //监听popWindow的dimiss事件
        lowDatePopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);//popWindow关闭之后恢复背景透明度

            }
        });
    }

    private void initLowDateView(){
        low_date_view = View.inflate(context, R.layout.plansetting_lowdate_view, null);
        Button date_cancel = (Button) low_date_view.findViewById(R.id.date_cancel);
        Button date_confirm = (Button) low_date_view.findViewById(R.id.date_confirm);
        final EditText et_day = (EditText) low_date_view.findViewById(R.id.et_day);

        date_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lowDatePopWindow.dismiss();
                lowDatePopWindow = null;
                backgroundAlpha(1f);
            }
        });

        date_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String least = context.getResources().getString(R.string.least);
                String day = context.getResources().getString(R.string.day);
                tv_least.setText(least + " " + et_day.getText().toString() + " " + day);
                lowDatePopWindow.dismiss();
                lowDatePopWindow = null;
                backgroundAlpha(1f);
            }
        });

    }




    /**
     * 设置屏幕的背景透明度
     * @param bgAlpha 透明度，范围0.0~1.0
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
    }

    /**
     * dp 2 px
     * @param dpVal
     */
    protected int dp2px(int dpVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }
}