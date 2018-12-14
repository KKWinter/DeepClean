package com.catchgift.trashclear.activity;

public class BatteryDataInfo {

    public double temperature;      //温度
    public String voltage;          //电压
    public int total;               //总电量
    public int current;             //当前电量

    private BatteryDataInfo() {
    }

    private static BatteryDataInfo batteryInfo = null;

    /**
     * 单例模式获取对象
     */
    public static BatteryDataInfo getBatteryInfo() {
        if (batteryInfo == null) {
            batteryInfo = new BatteryDataInfo();
        }
        return batteryInfo;
    }
}