package com.catchgift.trashclear.fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.catchgift.trashclear.R;
import com.catchgift.trashclear.cleanerfragment.ChildItem;
import com.catchgift.trashclear.cleanerfragment.CleanGroupDao;
import com.catchgift.trashclear.cleanerfragment.RubItemDao;
import com.catchgift.trashclear.cleanerfragment.CleanGroup;
import com.catchgift.trashclear.mainfragment.RubInfoProvider;
import com.catchgift.trashclear.utils.UMengStaticValue;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CleanerFragment extends Fragment {

    private ExpandableListView elv;
    private List<CleanGroup> mGroupList;
    private TextView tv_rubbish;
    private Button btn_clean;
    private TextView tv_clean;
    private CleanAdapter cleanAdapter;
    private View view;
    private Context context;
    private PackageManager pm;
    private List<ChildItem> allChildItem = new ArrayList<>();  //把所有的ChildItem装进去

    private Handler mHandler = new Handler() {
        private ArrayList<ChildItem> memList = new ArrayList<>();
        private ArrayList<ChildItem> sysCacheList = new ArrayList<>();
        private ArrayList<ChildItem> userCacheList = new ArrayList<>();
        private ArrayList<ChildItem> apkList = new ArrayList<>();
        private ArrayList<ChildItem> logList = new ArrayList<>();
        private long totalSize = 0;

        public void handleMessage(android.os.Message msg) {

            if (msg.what == 0) {  //检测垃圾的逻辑
                ChildItem childItem = (ChildItem) msg.obj;
                allChildItem.add(childItem);

                switch (childItem.itemTag) {
                    case "mem":
                        //累加清理出的大小
                        totalSize = totalSize + childItem.itemSize;
                        float a0 = (float) ((totalSize * 1.0) / (1024 * 1024));    //转换为mb
                        float b0 = (float) (Math.round(a0 * 100)) / 100;            //保留两位小数
                        String s0 = Float.toString(b0);
                        tv_rubbish.setText(s0);

                        //为父集合准备子集合数据
                        memList.add(childItem);

                        long memTotalSize = 0;
                        for (ChildItem c0 : memList) {
                            memTotalSize = memTotalSize + c0.itemSize;
                        }

                        CleanGroup memGroup = mGroupList.get(0);
                        memGroup.childList = memList;
                        memGroup.groupSize = memTotalSize;

                        //刷新listview
                        cleanAdapter.notifyDataSetChanged();

                        break;

                    case "sys":

                        //累加清理出的大小
                        totalSize = totalSize + childItem.itemSize;
                        float a1 = (float) ((totalSize * 1.0) / (1024 * 1024));    //转换为mb
                        float b1 = (float) (Math.round(a1 * 100)) / 100;            //保留两位小数
                        String s1 = Float.toString(b1);
                        tv_rubbish.setText(s1);

                        //为父集合准备子集合数据
                        sysCacheList.add(childItem);

                        long sysTotalSize = 0;
                        for (ChildItem c1 : sysCacheList) {
                            sysTotalSize = sysTotalSize + c1.itemSize;
                        }

                        CleanGroup sysCacheGroup = mGroupList.get(1);
                        sysCacheGroup.childList = sysCacheList;
                        sysCacheGroup.groupSize = sysTotalSize;

                        //刷新listview
                        cleanAdapter.notifyDataSetChanged();

                        break;

                    case "user":

                        totalSize = totalSize + childItem.itemSize;
                        float a2 = (float) ((totalSize * 1.0) / (1024 * 1024));
                        float b2 = (float) (Math.round(a2 * 100)) / 100;
                        String s2 = Float.toString(b2);
                        tv_rubbish.setText(s2);

                        userCacheList.add(childItem);

                        long userTotalSize = 0;
                        for (ChildItem c2 : userCacheList) {
                            userTotalSize = userTotalSize + c2.itemSize;
                        }

                        CleanGroup userCacheGroup = mGroupList.get(2);
                        userCacheGroup.childList = userCacheList;
                        userCacheGroup.groupSize = userTotalSize;

                        cleanAdapter.notifyDataSetChanged();

                        break;

                    case "apk":
                        totalSize = totalSize + childItem.itemSize;
                        float a3 = (float) ((totalSize * 1.0) / (1024 * 1024));
                        float b3 = (float) (Math.round(a3 * 100)) / 100;
                        String s3 = Float.toString(b3);
                        tv_rubbish.setText(s3);
                        apkList.add(childItem);

                        long apkTotalSize = 0;
                        for (ChildItem c3 : apkList) {
                            apkTotalSize = apkTotalSize + c3.itemSize;
                        }

                        CleanGroup apkGroup = mGroupList.get(3);
                        apkGroup.childList = apkList;
                        apkGroup.groupSize = apkTotalSize;

                        cleanAdapter.notifyDataSetChanged();

                        break;

                    case "log":

                        totalSize = totalSize + childItem.itemSize;
                        float a4 = (float) ((totalSize * 1.0) / (1024 * 1024));
                        float b4 = (float) (Math.round(a4 * 100)) / 100;
                        String s4 = Float.toString(b4);
                        tv_rubbish.setText(s4);

                        logList.add(childItem);

                        long logTotalSize = 0;
                        for (ChildItem c4 : logList) {
                            logTotalSize = logTotalSize + c4.itemSize;
                        }

                        CleanGroup logGroup = mGroupList.get(4);
                        logGroup.childList = logList;
                        logGroup.groupSize = logTotalSize;

                        cleanAdapter.notifyDataSetChanged();

                        break;

                    default:

                        break;
                }

            } else if (msg.what == 10) {                    //检测成功之后的逻辑

                tv_clean.setVisibility(View.GONE);        //按钮内容的转化
                btn_clean.setVisibility(View.VISIBLE);

                //先将头部垃圾大小递减
                for (int i = 0; i < 5; i++) {
                    elv.expandGroup(i);
                }


            } else if (msg.what == 100) {                //清理垃圾的逻辑

                ChildItem childItem = (ChildItem) msg.obj;
                //先将头部垃圾大小递减
                totalSize = totalSize - childItem.itemSize;
                float a = (float) ((totalSize * 1.0) / (1024 * 1024));
                float b = (float) (Math.round(a * 100)) / 100;
                String string = Float.toString(b);
                tv_rubbish.setText(string);

                //后台清理应用缓存
               // CleanAppCache.cleanAppCache(context);

                //再通过childItem的tag清理指定子集合中的数据
                switch (childItem.itemTag) {
                    case "mem":
                        //后台清理进程
                        String packageName = childItem.itemShow;
                        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                        am.killBackgroundProcesses(packageName);
                        //前台展示UI
                        memList.remove(childItem);
                        cleanAdapter.notifyDataSetChanged();

                        break;
                    case "sys":
                        sysCacheList.remove(childItem);
                        cleanAdapter.notifyDataSetChanged();

                        break;
                    case "user":
                        userCacheList.remove(childItem);
                        cleanAdapter.notifyDataSetChanged();

                        break;
                    case "apk":
                        String path = childItem.itemShow;
                        File apkFile = new File(path);
                        apkFile.delete();

                        apkList.remove(childItem);
                        cleanAdapter.notifyDataSetChanged();

                        break;
                    case "log":
                        RubInfoProvider rubInfoProvider = new RubInfoProvider();
                        List<File> logInfo = rubInfoProvider.getLogInfo();
                        for (File logFile : logInfo) {
                            logFile.delete();
                        }

                        logList.remove(childItem);
                        cleanAdapter.notifyDataSetChanged();

                        break;

                    default:
                        break;
                }
            } else if (msg.what == 1000) {
                tv_clean.setText(R.string.overclean);
            }
        }
    };


    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        view = View.inflate(context, R.layout.cleaner_fragment, null);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //回退按钮
        Button clean_back = (Button) view.findViewById(R.id.clean_back);
        clean_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                while (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStackImmediate();
                }

            }
        });


        //初始化控件对象
        tv_rubbish = (TextView) view.findViewById(R.id.tv_rubbish);
        btn_clean = (Button) view.findViewById(R.id.btn_clean);
        tv_clean = (TextView) view.findViewById(R.id.tv_clean);

        //展示可清理项【可扩展的listview】
        elv = (ExpandableListView) view.findViewById(R.id.elv);
        elv.setGroupIndicator(null);

        //父集合数据，写死的集合，共五项
        mGroupList = CleanGroupDao.getGroup(context);

        cleanAdapter = new CleanAdapter();
        elv.setAdapter(cleanAdapter);

        getData();

        //设置子条目的点击事件
        elv.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                btn_clean.setVisibility(View.VISIBLE);

                boolean checked = mGroupList.get(groupPosition).childList.get(childPosition).isChecked;
                mGroupList.get(groupPosition).childList.get(childPosition).isChecked = !checked;

                List<ChildItem> itemList = mGroupList.get(groupPosition).childList;
                long total = 0;
                for (ChildItem item : itemList) {
                    if (item.isChecked) {
                        total = total + item.itemSize;
                    }
                }
                mGroupList.get(groupPosition).groupSize = total;

                cleanAdapter.notifyDataSetChanged();

                return false;
            }
        });


        MobclickAgent.onEvent(context, UMengStaticValue.CLICK_CLEAN_WASTE_BTN);
        //设置清理按钮的点击事件
        btn_clean.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //变换按钮内容
                btn_clean.setVisibility(View.GONE);
                tv_clean.setVisibility(View.VISIBLE);
                tv_clean.setText(R.string.cleanning);

                new Thread() {
                    public void run() {
                        //临时集合，存放要删除的
                        List<ChildItem> tempList = new ArrayList<>();

                        //循环添加了所有childItem条目的大集合
                        for (int i = 0; i < allChildItem.size(); i++) {
                            ChildItem childItem = allChildItem.get(i);

                            if (childItem.isChecked) {
                                //判断是否选中清理
                                tempList.add(childItem);
                            }
                        }

                        for (ChildItem child : tempList) {
                            //大集合中移除
                            allChildItem.remove(child);

                            Message msg = Message.obtain();
                            msg.what = 100;
                            msg.obj = child;
                            mHandler.sendMessage(msg);

                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        Message msg = Message.obtain();
                        msg.what = 1000;
                        mHandler.sendMessage(msg);
                    }

                }.start();

            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    private long SLEEPTIME = 50;
    private void getData() {
        new Thread() {
            public void run() {
                pm = context.getPackageManager();

                ActivityManager am  = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                //获取正在运行的进程信息
                List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
                if (runningAppProcesses != null) {
                    for (ActivityManager.RunningAppProcessInfo appInfo : runningAppProcesses) {
                        if (appInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE){
                            String packageName = appInfo.processName;

                            //获取占用内存大小
                            Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{appInfo.pid});
                            Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
                            int processSize = memoryInfo.getTotalPrivateDirty()*1024;

                            ChildItem childItem = new ChildItem();
                            childItem.itemTag = "mem";
                            childItem.itemShow = packageName;
                            childItem.itemSize = processSize;
                            childItem.isChecked = true;

                            try {
                                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                                childItem.itemName = applicationInfo.loadLabel(pm).toString();
                                childItem.itemIcon = applicationInfo.loadIcon(pm);
                            } catch (NameNotFoundException e) {
                                e.printStackTrace();
                                childItem.itemName = packageName;
                                childItem.itemIcon = context.getResources().getDrawable(R.drawable.ic_launcher);
                            }

                            Message msg = Message.obtain();
                            msg.what = 0;
                            msg.obj = childItem;
                            mHandler.sendMessage(msg);

                            try {
                                Thread.sleep(SLEEPTIME);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


                List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
                for (PackageInfo packageInfo : installedPackages) {
                    String packageName = packageInfo.packageName;
                    try {
                        Class<?> clazz = Class.forName("android.content.pm.PackageManager");
                        Method method = clazz.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                        method.invoke(pm, packageName, mStatsObserver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(SLEEPTIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                List<ChildItem> apkData = RubItemDao.getApkData(context);
                if (apkData != null) {
                    for (ChildItem childItem : apkData) {

                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = childItem;
                        mHandler.sendMessage(msg);

                        try {
                            Thread.sleep(SLEEPTIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                List<ChildItem> logData = RubItemDao.getLogData(context);
                if (logData != null) {
                    for (ChildItem childItem : logData) {

                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = childItem;
                        mHandler.sendMessage(msg);

                        try {
                            Thread.sleep(SLEEPTIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }


                Message msg = Message.obtain();
                msg.what = 10;
                mHandler.sendMessage(msg);

            }

        }.start();

    }

    final IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {
        private ApplicationInfo applicationInfo;

        @Override
        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
            long cacheSize = stats.cacheSize;
            if (cacheSize > 0) {
                String packageName = stats.packageName;
                ChildItem childItem = new ChildItem();

                try {
                    applicationInfo = pm.getApplicationInfo(packageName, 0);
                    childItem.itemName = applicationInfo.loadLabel(pm).toString();
                    childItem.itemIcon = applicationInfo.loadIcon(pm);
                    childItem.itemShow = packageName;
                    childItem.itemSize = cacheSize;
                    childItem.isChecked = true;

                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }

                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    childItem.itemTag = "user";
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = childItem;
                    mHandler.sendMessage(msg);
                } else {
                    childItem.itemTag = "sys";
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = childItem;
                    mHandler.sendMessage(msg);
                }
            }
        }
    };


    class CleanAdapter extends BaseExpandableListAdapter {
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mGroupList.get(groupPosition).childList.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.cleaner_fragment_childitem, null);
            }

            ImageView iv_childicon = (ImageView) convertView.findViewById(R.id.iv_childicon);
            TextView tv_childtitle = (TextView) convertView.findViewById(R.id.tv_childtitle);
            CheckBox cb_ischecked = (CheckBox) convertView.findViewById(R.id.cb_ischecked);
            TextView tv_childcount = (TextView) convertView.findViewById(R.id.tv_childcount);

            ChildItem childItem = mGroupList.get(groupPosition).childList.get(childPosition);

            iv_childicon.setImageDrawable(childItem.itemIcon);
            tv_childtitle.setText(childItem.itemName);
            tv_childcount.setText(Formatter.formatFileSize(context, childItem.itemSize));
            cb_ischecked.setChecked(childItem.isChecked);

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mGroupList.get(groupPosition).childList.size();
        }


        @Override
        public Object getGroup(int groupPosition) {
            return mGroupList.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mGroupList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.cleaner_fragment_groupitem, null);
            }

            ImageView iv_groupicon = (ImageView) convertView.findViewById(R.id.iv_groupicon);
            TextView tv_grouptitle = (TextView) convertView.findViewById(R.id.tv_grouptitle);
            TextView tv_groupcount = (TextView) convertView.findViewById(R.id.tv_groupcount);
            ImageView iv_arrow = (ImageView) convertView.findViewById(R.id.iv_arrow);

            CleanGroup cleanGroup = mGroupList.get(groupPosition);

            iv_groupicon.setImageDrawable(cleanGroup.groupIcon);
            tv_grouptitle.setText(cleanGroup.groupName);

            List<ChildItem> childList = cleanGroup.childList;
            long total = 0;
            for (ChildItem item : childList) {
                if (item.isChecked) {
                    total = total + item.itemSize;
                }
            }

            tv_groupcount.setText(Formatter.formatFileSize(context, total));

            if (isExpanded) {
                iv_arrow.setImageResource(R.drawable.icon_up);
            } else {
                iv_arrow.setImageResource(R.drawable.icon_down);
            }


            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(UMengStaticValue.CLE_FGM);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(UMengStaticValue.CLE_FGM);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}