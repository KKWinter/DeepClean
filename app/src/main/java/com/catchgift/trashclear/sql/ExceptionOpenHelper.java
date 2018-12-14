package com.catchgift.trashclear.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ExceptionOpenHelper extends SQLiteOpenHelper {

    public ExceptionOpenHelper(Context context) {
        super(context, "YeahTools.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table IF NOT EXISTS battery"
                + "(name varchar(50),pkgName varchar(50),percent double,"
                + "value double,tcpBytesReceived double,tcpBytesSent double);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
