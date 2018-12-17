package com.ddong.qingjie.networkmonitor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ddong.qingjie.R;
import com.ddong.qingjie.view.MonitorProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Antony on 2016/1/9.
 */
public class FlowRankFragment extends Fragment{
    private Context context;
    private View view;

    private ListView lv_flow_rank;
    private List<AppInfo> appList;
    private RankAdapter rankAdapter;

    private TextView tv_rank_des;
    private ImageView iv_arrow;
    private RelativeLayout rl_sum_size;
    //下拉弹窗
    private PopupWindow popupWindow;
    private ListView mListView;
    private List<String> alist;
    //旋转进度条
    private ProgressBar pb_flowrank;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            pb_flowrank.clearAnimation();
            pb_flowrank.setVisibility(View.GONE);

            if(rankAdapter == null){
                rankAdapter = new RankAdapter();
                lv_flow_rank.setAdapter(rankAdapter);
            }else{
                rankAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        view = View.inflate(context, R.layout.flow_rank_fragment,null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //返回按钮设置
        Button flow_rank_back = (Button)view.findViewById(R.id.flow_rank_back);
        flow_rank_back.setOnClickListener(new View.OnClickListener() {
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



        //初始加载旋转进度条
        pb_flowrank = (ProgressBar)view.findViewById(R.id.pb_flowrank);

        //listview设置
        lv_flow_rank = (ListView)view.findViewById(R.id.lv_flow_rank);
        //准备数据
        getData();

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 获取数据集合
     */
    public void getData(){
        new Thread(){
            @Override
            public void run() {
                appList = FlowRankDao.getAllApps(context);

                //对list集合根据size大小排序
                Collections.sort(appList, new Comparator<AppInfo>() {
                    public int compare(AppInfo o1, AppInfo o2) {
                        if (o1.size < o2.size) {
                            return 1;
                        }
                        if (o1.size == o2.size) {
                            return 0;
                        }
                        return -1;
                    }
                });
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    class RankAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public Object getItem(int position) {
            return appList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null){
                convertView = View.inflate(context, R.layout.flow_rank_item, null);
                viewHolder = new ViewHolder();

                viewHolder.iv_app_icon = (ImageView)convertView.findViewById(R.id.iv_app_icon);
                viewHolder.tv_app_name = (TextView)convertView.findViewById(R.id.tv_app_name);
                viewHolder.pb_monitor = (MonitorProgressBar)convertView.findViewById(R.id.pb_monitor);

                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //设置图标和名称
            viewHolder.iv_app_icon.setImageDrawable(appList.get(position).icon);
            viewHolder.tv_app_name.setText(appList.get(position).name);

            //设置进度条
            long current = appList.get(position).size;
            long total = appList.get(0).size;
            viewHolder.pb_monitor.setMax((int) total);
            viewHolder.pb_monitor.setProgress((int) current);

            String flow_size = Formatter.formatFileSize(context, appList.get(position).size);
            viewHolder.pb_monitor.setText(flow_size);

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView iv_app_icon;
        TextView tv_app_name;
        MonitorProgressBar pb_monitor;
    }

    /**
     * 弹出一个下拉窗体
     */
    private void showPopupWindow() {
        initListView();

        // 创建PopupWindow对象，指定内容为ListView，宽度为输入框的宽度，高度为200，当前窗体可以获取焦点
        popupWindow = new PopupWindow(mListView, rl_sum_size.getWidth(), rl_sum_size.getHeight()*3 + 6, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAsDropDown(rl_sum_size, 4, -4);
    }

    /**
     * 初始化ListView
     */
    private void initListView() {
        mListView = new ListView(context);
        mListView.setVerticalScrollBarEnabled(false); // 隐藏垂直滚动条
        mListView.setBackgroundColor(Color.WHITE);

        //准备数据
        alist = new ArrayList<>();
        alist.add("昨日流量");
        alist.add("当天流量");
        alist.add("当月流量");

        PopAdapter popAdapter = new PopAdapter();
        mListView.setAdapter(popAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                popupWindow.dismiss(); //关闭

            }
        });
    }

    class PopAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return alist.size();
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = View.inflate(context, R.layout.pop_list_item, null);
            }
            TextView tv = (TextView) convertView.findViewById(R.id.tv);
            tv.setText(alist.get(position));
            return convertView;
        }
        @Override
        public Object getItem(int position) {
            return alist.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }


    }

}
