package com.ddong.qingjie.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ddong.qingjie.batteryfragment.BattRankInfo;

import java.util.ArrayList;
import java.util.List;

public class ExceptionDao {
    //单例模式
    private ExceptionOpenHelper mExceptionOpenHelper;

    private ExceptionDao(Context context) {
        //要在创建ExceptionDao对象的过程中,就去生成数据库以及表,在此次创建ExceptionOpenHelper对象
        mExceptionOpenHelper = new ExceptionOpenHelper(context);
    }

    private static ExceptionDao mExceptionDao = null;

    public static ExceptionDao getInstance(Context context) {
        if (mExceptionDao == null) {
            mExceptionDao = new ExceptionDao(context);
        }

        return mExceptionDao;
    }

    //插入一个sipper条目
    public void insert(BattRankInfo sipper) {
        SQLiteDatabase db = mExceptionOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", sipper.getName());
        contentValues.put("pkgName", sipper.getPkgName());
        contentValues.put("percent", sipper.getPercentOfTotal());
        contentValues.put("value", sipper.getValue());
        contentValues.put("tcpBytesReceived", sipper.getTcpBytesReceived());
        contentValues.put("tcpBytesSent", sipper.getTcpBytesSent());

        db.insert("battery", null, contentValues);

        db.close();
    }


    //根据sipper的name，删除一个条目
    public void delete(BattRankInfo sipper) {
        SQLiteDatabase db = mExceptionOpenHelper.getWritableDatabase();

        db.delete("battery", "name = ?", new String[]{sipper.getName()});

        db.close();
    }

    /**
     * 查询sipper条目是否在数据库中
     * @param  sipper  条目的javabean对象
     * @return boolean
     */
    public boolean findName(BattRankInfo sipper) {
        SQLiteDatabase db = mExceptionOpenHelper.getWritableDatabase();
        boolean isExist = false;

        Cursor cursor = db.query("battery", new String[]{"name"}, "name = ?", new String[]{sipper.getName()}, null, null, null);
        if (cursor != null) {
            int count = cursor.getCount();
            if (count != 0) {
                isExist = true;
            }
        }

        cursor.close();
        db.close();
        return isExist;
    }

    /**
     * 查询数据库中的所有条目集合
     * @return List<BattRankInfo>
     */
    public List<BattRankInfo> findAll() {
        List<BattRankInfo> aList = new ArrayList<>();

        SQLiteDatabase db = mExceptionOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from battery;", null);
        while (cursor.moveToNext()) {
            BattRankInfo sipper = new BattRankInfo();

            sipper.setName(cursor.getString(cursor.getColumnIndex("name")));
            sipper.setPkgName(cursor.getString(cursor.getColumnIndex("pkgName")));
            aList.add(sipper);
        }

        cursor.close();
        db.close();
        return aList;
    }
}