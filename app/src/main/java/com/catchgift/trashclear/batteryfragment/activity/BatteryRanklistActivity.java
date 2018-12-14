package com.catchgift.trashclear.batteryfragment.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.catchgift.trashclear.R;
import com.catchgift.trashclear.batteryfragment.BattRankInfo;
import com.catchgift.trashclear.view.MonitorProgressBar;

import com.catchgift.trashclear.batteryfragment.BattRankInfoProvider;
import com.catchgift.trashclear.utils.UMengStaticValue;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

public class BatteryRanklistActivity extends Activity {

    private Context mContext;
    private ListView mRanklist;
    private customAdapter adapter;
    private BattRankInfoProvider info;
    private List<BattRankInfo> mList;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (adapter == null) {
                adapter = new customAdapter();
                mRanklist.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_ranklist);
        mContext = this;

        initUI();
    }

    private void initUI() {
        Button power_rank_back = (Button) findViewById(R.id.power_rank_back);
        power_rank_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRanklist = (ListView) findViewById(R.id.rank_list);

        //获取数据
        getBatteryStats();

        //条目的点击事件
        mRanklist.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, ParticularsActivity.class);
                intent.putExtra("BatterySipper", mList.get(position));
                startActivity(intent);
            }

        });
    }


    //获取电池状态
    private void getBatteryStats() {
        new Thread() {
            public void run() {

                info = new BattRankInfoProvider(mContext);
                info.setMinPercentOfTotal(0.1);
                mList = info.getBatteryStats();
                if (mList != null) {
                    mHandler.sendEmptyMessage(0);
                }
            }
        }.start();
    }

    class customAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public customAdapter() {
            inflater = LayoutInflater.from(mContext);

            /*
			for (int i = mList.size() - 1; i >= 0; i--) {
				BattRankInfo sipper = mList.get(i);
				String name = sipper.getName();
				Drawable icon = sipper.getIcon();
				String pkgName=sipper.getPkgName();

				if (name!= null) {
					sipper.setName(name);
					sipper.setIcon(icon);
					sipper.setPkgName(pkgName);
				} else {
					mList.remove(i);
				}
			}
			notifyDataSetInvalidated();
			*/
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public BattRankInfo getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                holder = new Holder();
                convertView = inflater.inflate(R.layout.ranklist_item, null);

                holder.appIcon = (ImageView) convertView.findViewById(R.id.ranklist_image);
                holder.appName = (TextView) convertView.findViewById(R.id.ranklist_name);
                holder.pb_ranking = (MonitorProgressBar) convertView.findViewById(R.id.battery_ranklist_progress);

                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            //给UI赋值
            BattRankInfo sipper = getItem(position);

            holder.appIcon.setImageDrawable(sipper.getIcon());
            holder.appName.setText(sipper.getName());

            double percentOfTotal = sipper.getPercentOfTotal();
            holder.pb_ranking.setProgress((int) percentOfTotal);
            holder.pb_ranking.setText(format(percentOfTotal));

            return convertView;
        }
    }

    class Holder {
        ImageView appIcon;
        TextView appName;
        MonitorProgressBar pb_ranking;
    }

    private String format(double size) {
        return String.format("%1$.2f%%", size);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Adjust.onResume();
        MobclickAgent.onResume(this);          //统计时长
        MobclickAgent.onPageStart(UMengStaticValue.BATT_ACT_RANK);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Adjust.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(UMengStaticValue.BATT_ACT_PART);
    }
}
