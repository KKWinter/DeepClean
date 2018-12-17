package com.ddong.qingjie.cleanerfragment;

import android.content.Context;

import com.ddong.qingjie.R;
import com.ddong.qingjie.mainfragment.ApkInfo;
import com.ddong.qingjie.mainfragment.RubInfoProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RubItemDao {

    public static List<ChildItem> getApkData(Context context) {

        //无用apk的集合
        RubInfoProvider rubInfoProvider = new RubInfoProvider();
        List<ApkInfo> apkInfoList = rubInfoProvider.getApkInfo(context);

        ArrayList<ChildItem> aList = new ArrayList<>();
        for (ApkInfo apkInfo : apkInfoList) {
            ChildItem childItem = new ChildItem();

            childItem.itemTag = "apk";
            childItem.itemIcon = apkInfo.icon;
            childItem.itemName = apkInfo.name;
            childItem.itemSize = apkInfo.size;
            childItem.isChecked = true;
            childItem.itemShow = apkInfo.path;

            aList.add(childItem);
        }

        return aList;
    }

    public static List<ChildItem> getLogData(Context context) {

        //垃圾文件的集合
        ArrayList<ChildItem> fList = new ArrayList<>();

        long totalLogSize = 0;
        RubInfoProvider rubInfoProvider = new RubInfoProvider();
        List<File> logInfo = rubInfoProvider.getLogInfo();
        for (File file : logInfo) {
            if (file != null) {
                totalLogSize = totalLogSize + file.length();
            }
        }
        ChildItem childItem = new ChildItem();

        childItem.itemTag = "log";
        childItem.itemIcon = context.getResources().getDrawable(R.drawable.icon_rubbish);
        childItem.itemName = context.getResources().getString(R.string.rubbishfile);
        childItem.itemSize = totalLogSize;
        childItem.isChecked = true;

        if (childItem.itemSize > 0) {
            fList.add(childItem);
        }
        return fList;
    }
}