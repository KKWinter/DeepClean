package com.ddong.qingjie.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddong.qingjie.utils.YLog;
import com.slidingmenu.lib.SlidingMenu;

public abstract class BaseFragment extends Fragment {
    public View view;
    public Context context;
    public SlidingMenu slidingMenu;

    public BaseFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        YLog.d("Base OnCreate");
    }

    //xml-->view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = initView();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initData();
        super.onActivityCreated(savedInstanceState);
    }

    //将每一个子fragment对象中的布局转换成view对象
    public abstract View initView();

    //拿数据填充oncreateView返回的view对象
    public abstract void initData();
}