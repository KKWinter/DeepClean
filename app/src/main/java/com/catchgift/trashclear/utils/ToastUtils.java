package com.catchgift.trashclear.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private static Toast toast;

    public static void show(Context ctx, String message) {
        if (toast == null) {
            toast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
        }
        toast.setText(message);
        toast.show();
    }
}