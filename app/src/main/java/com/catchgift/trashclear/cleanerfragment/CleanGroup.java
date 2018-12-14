package com.catchgift.trashclear.cleanerfragment;


import java.util.List;

import android.graphics.drawable.Drawable;

public class CleanGroup {

    public Drawable groupIcon;          //图标
    public String groupName;            //名称
    public long groupSize;              //大小
    public List<ChildItem> childList;   //扩展项
}
