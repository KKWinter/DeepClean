package com.catchgift.trashclear.cleanerfragment;

import android.content.Context;


import com.catchgift.trashclear.R;

import java.util.ArrayList;
import java.util.List;

public class CleanGroupDao {
	
	public static List<CleanGroup> getGroup(Context context){
		List<CleanGroup> groupList= new ArrayList<>();
		
		
//		CleanGroup cleanGroup = new CleanGroup();
//		cleanGroup.groupIcon = context.getResources().getDrawable(R.drawable.icon_cache);
//		cleanGroup.groupName = "缓存冗余";
//		cleanGroup.groupSize = 0;
//		cleanGroup.childList = aList;
//		groupList.add(cleanGroup);

		ArrayList<ChildItem> ayList = new ArrayList<>();
		
		
		CleanGroup cleanGroup1 = new CleanGroup();
		cleanGroup1.groupIcon = context.getResources().getDrawable(R.drawable.icon_memory);
		cleanGroup1.groupName = context.getResources().getString(R.string.groupname1);
		cleanGroup1.childList = ayList;
		cleanGroup1.groupSize = 0;
		groupList.add(cleanGroup1);
		
		
		CleanGroup cleanGroup2 = new CleanGroup();
		cleanGroup2.groupIcon = context.getResources().getDrawable(R.drawable.icon_system);
		cleanGroup2.groupName = context.getResources().getString(R.string.groupname2);
		cleanGroup2.childList = ayList;
		cleanGroup2.groupSize = 0;
		groupList.add(cleanGroup2);
		
		
		CleanGroup cleanGroup3 = new CleanGroup();
		cleanGroup3.groupIcon = context.getResources().getDrawable(R.drawable.icon_app);
		cleanGroup3.groupName = context.getResources().getString(R.string.groupname3);
		cleanGroup3.childList = ayList;
		cleanGroup3.groupSize = 0;
		groupList.add(cleanGroup3);
		
		
		CleanGroup cleanGroup4 = new CleanGroup();
		cleanGroup4.groupIcon = context.getResources().getDrawable(R.drawable.icon_package);
		cleanGroup4.groupName = context.getResources().getString(R.string.groupname4);
		cleanGroup4.childList = ayList;
		cleanGroup4.groupSize = 0;
		groupList.add(cleanGroup4);
		
		
		CleanGroup cleanGroup5 = new CleanGroup();
		cleanGroup5.groupIcon = context.getResources().getDrawable(R.drawable.icon_file);
		cleanGroup5.groupName = context.getResources().getString(R.string.groupname5);
		cleanGroup5.childList = ayList;
		cleanGroup5.groupSize = 0;
		groupList.add(cleanGroup5);
		
		
		return groupList;
	}
}