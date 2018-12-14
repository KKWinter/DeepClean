package com.catchgift.trashclear.sys;


import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.os.RemoteException;

import java.lang.reflect.Method;

public class CleanAppCache {

    public static void cleanAppCache(Context context,String packageName) {
        PackageManager mPM = context.getPackageManager();

        /**
         * 清理所有应用的缓存,
         * 手机已经由缓存占有了一定空间,但是此时管手机要Long.MAX_VALUE空间,手机是肯定没有这么大的空间去提供,
         * 隐藏方法
         */

        try {
            Class<?> clazz = Class.forName("android.content.pm.PackageManager");
            Method method = clazz.getMethod("freeStorageAndNotify", long.class,
                    IPackageDataObserver.class);
            method.invoke(mPM, Long.MAX_VALUE, new IPackageDataObserver.Stub() {
                @Override
                public void onRemoveCompleted(String packageName, boolean succeeded)
                        throws RemoteException {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}