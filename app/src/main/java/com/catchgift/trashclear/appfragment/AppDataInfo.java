package com.catchgift.trashclear.appfragment;

import android.graphics.drawable.Drawable;

/*
 * app实锟斤拷锟斤拷
 * 
 * */
public class AppDataInfo {
	
	public Drawable appDrawable;		//图片
	public String appName; 				//名字
	public String packageName;			//包名
	public long cacheSize;
	public long codeSize;
	public long dataSize;
	public long totalsize;				//总大小
	public boolean ischeck; 			//是否选中
	public boolean isSystem; 			//是否系统应用
	
	public AppDataInfo() {
		super();
	}


	public Drawable getAppDrawable() {
		return appDrawable;
	}
	public void setAppDrawable(Drawable appDrawable) {
		this.appDrawable = appDrawable;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public Long getTotalsize() {
		return totalsize;
	}
	public void setTotalsize(long totalsize) {
		this.totalsize = totalsize;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}


	@Override
	public String toString() {
		return "Applications [appDrawable=" + appDrawable + ", appName=" + appName + ", packageName=" + packageName
				+ ", totalsize=" + totalsize + "]";
	}
	
}
