package com.catchgift.trashclear.cleanerfragment;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Debug.MemoryInfo;

import com.catchgift.trashclear.R;

import java.util.ArrayList;
import java.util.List;

public class MemItemDao {
	
	public static List<ChildItem> getMemData(Context context){
		//进程的集合
		ArrayList<ChildItem> pList = new ArrayList<>();

		ActivityManager am  = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = context.getPackageManager();

		//获取正在运行的进程信息
		List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
		if (runningAppProcesses != null) {
			for (RunningAppProcessInfo appInfo : runningAppProcesses) {
				if (appInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE){
					String packageName = appInfo.processName;
					
					MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{appInfo.pid});
					MemoryInfo memoryInfo = processMemoryInfo[0];
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
					pList.add(childItem);
					
				}
			}
		}
		return pList;
	}
}
