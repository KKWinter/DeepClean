package com.ddong.qingjie.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.ddong.qingjie.R;
import com.ddong.qingjie.appfragment.SystemFragment;
import com.ddong.qingjie.appfragment.UserFragment;
import com.ddong.qingjie.utils.UMengStaticValue;
import com.umeng.analytics.MobclickAgent;

public class AppFragment extends Fragment {

    private static Context context;
    private View view;
    private ViewPager app_viewpager;
    private Button app_button_uninstall;
    public static final String ARGUMENTS_NAME = "arg";
    private LayoutInflater mInflater;
    private RadioGroup app_text;
    private ImageView iv_nav_indicator;
    private int indicatorWidth;
    private int currentIndicatorLeft = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        view = inflater.inflate(R.layout.app_fragment, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Button app_back = (Button) view.findViewById(R.id.app_back);
        app_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭自己

                FragmentManager fragmentManager = getFragmentManager();
                while (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate();
                }

            }
        });


        //初始化控件对象
        initUI();

        //设置viewpager指针
        setViewPagerIndicator();

        //设置按钮和viewpager的监听
        setListener();

        super.onActivityCreated(savedInstanceState);
    }


    private void initUI() {
        app_viewpager = (ViewPager) view.findViewById(R.id.app_viewpager);
        app_button_uninstall = (Button) view.findViewById(R.id.app_button_uninstall);
        app_text = (RadioGroup) view.findViewById(R.id.app_text);
        iv_nav_indicator = (ImageView) view.findViewById(R.id.app_text_indicator);
    }


    private void setViewPagerIndicator() {
        //获取屏幕宽度
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        indicatorWidth = dm.widthPixels / 2;

        LayoutParams cursor_Params = iv_nav_indicator.getLayoutParams();
        cursor_Params.width = indicatorWidth;// 初始化滑动下标的宽
        iv_nav_indicator.setLayoutParams(cursor_Params);

        initNavigationHSV();

        //设置viewpager的数据适配
        app_viewpager.setAdapter(new TabFragmentPagerAdapter(getChildFragmentManager()));
    }

    private void setListener() {
        //卸载按钮
        app_button_uninstall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //通知删除
                UserFragment.notifyDelete();
            }
        });

        //监听指针
        app_text.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (app_text.getChildAt(checkedId) != null) {
//					RadioButton rb=(RadioButton)view.findViewById(checkedId);
//					rb.setChecked(true);
                    if (checkedId == 0) {
                        app_text.getChildAt(0).setSelected(true);
                    } else {
                        app_text.getChildAt(0).setSelected(false);
                    }
                    TranslateAnimation animation = new TranslateAnimation(currentIndicatorLeft,
                            app_text.getChildAt(checkedId).getLeft(), 0f, 0f);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.setDuration(100);
                    animation.setFillAfter(true);

                    // 执行位移动画
                    iv_nav_indicator.startAnimation(animation);

                    app_viewpager.setCurrentItem(checkedId); // ViewPager 跟随一起 切换

                }

            }
        });

        //监听viewpager，让指针跟着一起动
        app_viewpager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (app_text != null && app_text.getChildCount() > position) {
                    app_text.getChildAt(position).performClick();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void initNavigationHSV() {
        app_text.removeAllViews();
        String userapp = context.getResources().getString(R.string.userapp);
        String sysapp = context.getResources().getString(R.string.sysapp);
        String[] tabtitle = {userapp, sysapp};

        for (int i = 0; i < tabtitle.length; i++) {
            RadioButton rb = (RadioButton) LayoutInflater.from(getActivity()).inflate(R.layout.app_radiogroup_item, null);
            rb.setId(i);
            rb.setText(tabtitle[i]);
            rb.setTextSize(17f);
            rb.setLayoutParams(new LayoutParams(indicatorWidth, LayoutParams.MATCH_PARENT));
            rb.setChecked(false);
            app_text.addView(rb);
        }

        app_text.getChildAt(0).setSelected(true);
    }


    // Viewpager适配fragement
    public static class TabFragmentPagerAdapter extends FragmentPagerAdapter {

        public TabFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            Fragment fragment = null;
            switch (arg0) {
                case 0:
                    fragment = new UserFragment();
                    break;

                case 1:
                    fragment = new SystemFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {

            return 2;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageStart(UMengStaticValue.APP_FGM);
    }

    @Override
    public void onStop() {
        super.onStop();
        MobclickAgent.onPageEnd(UMengStaticValue.APP_FGM);
    }
}