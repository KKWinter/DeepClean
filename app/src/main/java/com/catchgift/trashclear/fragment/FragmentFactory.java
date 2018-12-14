package com.catchgift.trashclear.fragment;

import android.support.v4.app.Fragment;


public class FragmentFactory {

    private static MainFragment mainFragment = null;
    private static CleanerFragment cleanFragment = null;
    private static AppFragment appFragment = null;
    private static BatteryFragment batteryFragment = null;

    public static Fragment getInstanceByIndex(int index) {
        Fragment fragment = null;
        switch (index) {
            case 1:
                if (mainFragment == null) {
                    mainFragment = new MainFragment();
                }
                fragment = mainFragment;
                break;

            case 2:
                if (cleanFragment == null) {
                    cleanFragment = new CleanerFragment();
                }
                fragment = cleanFragment;
                break;
            case 3:
                if (appFragment == null) {
                    appFragment = new AppFragment();
                }
                fragment = appFragment;
                break;
            case 4:
                if (batteryFragment == null) {
                    batteryFragment = new BatteryFragment();
                }
                fragment = batteryFragment;
                break;
        }

        return fragment;
    }
}
