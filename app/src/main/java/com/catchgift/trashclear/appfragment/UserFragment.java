package com.catchgift.trashclear.appfragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.catchgift.trashclear.R;
import com.catchgift.trashclear.ads.Config;
import com.catchgift.trashclear.ads.DCTAdEventListener;
import com.catchgift.trashclear.utils.ToastUtils;
import com.catchgift.trashclear.utils.UMengStaticValue;
import com.cloudtech.ads.core.CTNative;
import com.cloudtech.ads.core.CTService;
import com.cloudtech.ads.utils.ContextHolder;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class UserFragment extends Fragment {
    private ListView app_listview;
    private ProgressBar pb_user;

    private static ArrayList<AppDataInfo> arrayList;
    private static MyAppListAdpater myAppListAdapter;
    private View view;
    private static Context mContext;
    private static UserFragment userfragment;
    private AppDataThread appDataThread;
    private RelativeLayout rl_ad_container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        userfragment = this;
        mContext = getActivity();
        view = inflater.inflate(R.layout.app_user_fragment, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        app_listview = (ListView) view.findViewById(R.id.app_list);
        pb_user = (ProgressBar) view.findViewById(R.id.pb_user);
        rl_ad_container = (RelativeLayout) view.findViewById(R.id.rl_ad_container);

        //准备数据
        arrayList = new ArrayList<>();
        myAppListAdapter = new MyAppListAdpater(mContext, arrayList);
        app_listview.setAdapter(myAppListAdapter);

        //异步监听获取app数据
        appDataThread = InitAppData.getAppInfo(new InitAppData.AppDataListener() {
            @Override
            public void userAppDataListener(AppDataInfo appInfo) {
                //TODO arrayList = null
                if (arrayList != null){
                    pb_user.clearAnimation();
                    pb_user.setVisibility(View.GONE);
                    arrayList.add(appInfo);
                    myAppListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void sysAppDataListener(AppDataInfo appInfo) {
            }
        }, mContext);



        //获取广告的接口
        CTService.getNative(Config.slotId_Media, true,
                ContextHolder.getContext(), false, new DCTAdEventListener(){
                    @Override
                    public void onAdviewGotAdSucceed(CTNative result) {
                        if (result != null){
                            rl_ad_container.setVisibility(View.VISIBLE);
                            rl_ad_container.addView(result);
                        }

                        super.onAdviewGotAdSucceed(result);
                    }

                    @Override
                    public void onAdviewGotAdFail(CTNative result) {
                        super.onAdviewGotAdFail(result);
                    }

                    @Override
                    public void onAdviewClicked(CTNative result) {
                        super.onAdviewClicked(result);
                    }

                    @Override
                    public void onAdviewClosed(CTNative result) {
                        rl_ad_container.removeAllViews();
                        rl_ad_container.setVisibility(View.GONE);

                        super.onAdviewClosed(result);
                    }
                });


        //listview条目的点击事件
        app_listview.setOnItemClickListener(new OnItemClickListener() {

            private AppDataInfo appDataInfo;

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                if (arrayList.get(position).packageName.equals(mContext.getPackageName())) {
                    //不让卸载当前应用
                    arrayList.get(position).ischeck = false;
                } else {
                    appDataInfo = arrayList.get(position);

                    if (appDataInfo != null) {
                        //集合中数据的改变
                        appDataInfo.ischeck = !appDataInfo.ischeck;

                        //UI效果的切换
                        CheckBox cb_box = (CheckBox) view.findViewById(R.id.app_list_checkbox);
                        cb_box.setChecked(appDataInfo.ischeck);

//                        myAppListAdapter.notifyDataSetChanged();
                    }
                }

            }
        });

        super.onActivityCreated(savedInstanceState);
    }


    public static void notifyDelete() {

        //获取选中要删除的集合
        ArrayList<AppDataInfo> deleteList = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            AppDataInfo appDataInfo = arrayList.get(i);

            if (appDataInfo.packageName.equals(mContext.getPackageName())) {
                continue;
            }
            if (appDataInfo.ischeck) {

                deleteList.add(appDataInfo);
            }
        }

        if (deleteList.size() != 0) {

            for (int i = 0; i < deleteList.size() - 1; i++) {
                AppDataInfo appDataInfo = deleteList.get(i);

                Uri packageURI = Uri.parse("package:" + appDataInfo.packageName);
                Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
                mContext.startActivity(intent);
            }

            AppDataInfo appDataInfo = deleteList.get(deleteList.size() - 1);
            Uri packageURI = Uri.parse("package:" + appDataInfo.packageName);
            Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //在依附的activity中获取卸载界面返回的结果
            userfragment.getActivity().startActivityForResult(intent, Activity.RESULT_FIRST_USER);

        } else {
            String str = mContext.getResources().getString(R.string.noChoice);
            ToastUtils.show(mContext, str);
        }
    }

    public static void notifyUpdate() {
        //接到了卸载后的通知，如何刷新数据
        //先清空数据集合
        arrayList.clear();
        myAppListAdapter.notifyDataSetChanged();
        //再异步获取数据去填充
        InitAppData.getAppInfo(new InitAppData.AppDataListener() {
            @Override
            public void userAppDataListener(AppDataInfo appInfo) {
                arrayList.add(appInfo);

                myAppListAdapter.notifyDataSetChanged();
            }

            @Override
            public void sysAppDataListener(AppDataInfo appInfo) {}
        }, mContext);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UMengStaticValue.APP_FGM_USER);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UMengStaticValue.APP_FGM_USER);
    }

    @Override
    public void onDestroy() {
        appDataThread.closeThread = true;
        pb_user.setVisibility(View.GONE);
        arrayList = null;
        super.onDestroy();
    }
}