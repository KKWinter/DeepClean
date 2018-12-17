package com.ddong.qingjie.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class SaveUtils {
    public static boolean saveUserInfo(Context context, boolean isStart) {
        try {
            //布尔型转为字符型
            String info = String.valueOf(isStart);
            FileOutputStream outputStream = context.openFileOutput("info.txt", Context.MODE_PRIVATE);

            outputStream.write(info.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean getUserInfo(Context context) {
        try {
            FileInputStream inputStream = context.openFileInput("info.txt");

            //创建一个读取一行的流
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
            String result = bfr.readLine();

            return Boolean.valueOf(result);//字符串转换为波尔型，并返回

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
