package com.catchgift.trashclear.appfragment;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.catchgift.trashclear.R;

import java.util.ArrayList;

public class MyAppListAdpater extends BaseAdapter {
    private Context mContext;
    private ArrayList<AppDataInfo> list;

    public MyAppListAdpater(Context mContext, ArrayList<AppDataInfo> list) {
        super();
        this.mContext = mContext;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public AppDataInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vholder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.app_listview, null);

                vholder = new ViewHolder();

                vholder.app_list_image = (ImageView) convertView.findViewById(R.id.app_list_image);
                vholder.app_list_name = (TextView) convertView.findViewById(R.id.app_list_name);
                vholder.app_list_bytes = (TextView) convertView.findViewById(R.id.app_list_bytes);
                vholder.app_list_checkbox = (CheckBox) convertView.findViewById(R.id.app_list_checkbox);

                convertView.setTag(vholder);
            } else {

                vholder = (ViewHolder) convertView.getTag();
            }

            vholder.app_list_image.setImageDrawable(list.get(position).getAppDrawable());
            vholder.app_list_name.setText(list.get(position).getAppName());
            vholder.app_list_bytes.setText(Formatter.formatFileSize(mContext, list.get(position).getTotalsize()));

            //判断是否当前应用和系统应用，它们不能被选中，把复选框去掉
            if (list.get(position).packageName.equals(mContext.getPackageName())) {
                vholder.app_list_checkbox.setVisibility(View.INVISIBLE);
            } else if (list.get(position).isSystem) {
                vholder.app_list_checkbox.setVisibility(View.GONE);
            } else {
                vholder.app_list_checkbox.setVisibility(View.VISIBLE);
            }

            vholder.app_list_checkbox.setChecked(list.get(position).ischeck);

            return convertView;

    }

    private class ViewHolder {
        ImageView app_list_image;
        TextView app_list_name, app_list_bytes;
        CheckBox app_list_checkbox;
    }

}
