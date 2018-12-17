package com.ddong.qingjie.appfragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.ddong.qingjie.R;
import com.ddong.qingjie.utils.UMengStaticValue;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class SystemFragment extends Fragment {
    private ListView app_listview; // TODO Fix
    private ArrayList<AppDataInfo> arrayList;
    private Context context;
    private MyAppListAdpater myAppListAdapter;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        view = inflater.inflate(R.layout.app_sys_fragment, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        app_listview = (ListView) view.findViewById(R.id.sys_list);

        //准备数据
        arrayList = new ArrayList<>();

        myAppListAdapter = new MyAppListAdpater(context, arrayList);

        app_listview.setAdapter(myAppListAdapter);

        //准备数据
        InitAppData.getAppInfo(new InitAppData.AppDataListener() {
            @Override
            public void userAppDataListener(AppDataInfo appInfo) {
            }

            @Override
            public void sysAppDataListener(AppDataInfo appInfo) {
                arrayList.add(appInfo);
                myAppListAdapter.notifyDataSetChanged();

            }
        }, context);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UMengStaticValue.APP_FGM_SYSTEM);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UMengStaticValue.APP_FGM_USER);
    }
}
