package com.catchgift.trashclear.utils;

import android.util.Log;

public class YLog {

    private final static String TAG = "DEBUG_CT";

    private YLog() {
    }

    public static void i(String msg) {
        if (msg != null) {
            Log.i(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (msg != null) {
            Log.d(TAG, msg);
        }
    }

    public static void w(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
        }
    }

    public static void e(String msg) {
        if (msg != null) {
            Log.e(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (msg != null) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (msg != null) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (msg != null) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (msg != null) {
            Log.e(tag, msg);
        }
    }
}
