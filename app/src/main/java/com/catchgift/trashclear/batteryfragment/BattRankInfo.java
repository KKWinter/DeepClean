package com.catchgift.trashclear.batteryfragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.BatteryStats.Uid;
import android.os.Parcel;
import android.os.Parcelable;

import com.catchgift.trashclear.batteryfragment.BattRankInfoProvider.DrainType;

import java.util.HashMap;

public class BattRankInfo implements Comparable<BattRankInfo>, Parcelable {
    private static Context mContext;
    private final HashMap<String, UidToDetail> mUidCache = new HashMap<>();

    private String name;
    private String pkgName;
    private Drawable icon;
    private Uid uidObj;
    private double value;
    private double[] values;
    long usageTime;
    long cpuTime;
    long gpsTime;
    long wifiRunningTime;
    long cpuFgTime;
    long wakeLockTime;
    long tcpBytesReceived;
    long tcpBytesSent;
    private double percent;
    double noCoveragePercent;
    private String defaultPackageName;
    private DrainType drainType;
    private String bitmap;

    static class UidToDetail {
        String name;
        String packageName;
        Drawable icon;
    }

    public BattRankInfo(Context context, String pkgName, double time) {
        mContext = context;
        value = time;
        drainType = DrainType.APP;
        getQuickNameIcon(pkgName);
    }

    public BattRankInfo(Context context, BattRankInfoProvider.DrainType type, Uid uid, double[] _values) {
        mContext = context;
        values = _values;
        drainType = type;
        if (values != null)
            value = values[0];

        uidObj = uid;

        if (uid != null) {
            getQuickNameIconForUid(uid);
        }
    }


    public BattRankInfo() {

    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getPercentOfTotal() {
        return percent;
    }

    @Override
    public int compareTo(BattRankInfo other) {
        return (int) (other.getValue() - getValue());
    }

    public String getBitmap() {
        return bitmap;
    }

    public long getTcpBytesReceived() {
        return tcpBytesReceived;
    }

    public long getTcpBytesSent() {
        return tcpBytesSent;
    }

    private void getQuickNameIcon(String pkgName) {
        PackageManager pm = mContext.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            icon = appInfo.loadIcon(pm);                // pm.getApplicationIcon(appInfo);
            name = appInfo.loadLabel(pm).toString();    // pm.getApplicationLabel(appInfo).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getQuickNameIconForUid(Uid uidObj) {
        final int uid = uidObj.getUid();
        final String uidString = Integer.toString(uid);
        if (mUidCache.containsKey(uidString)) {
            UidToDetail utd = mUidCache.get(uidString);
            defaultPackageName = utd.packageName;
            name = utd.name;
            icon = utd.icon;
            return;
        }
        PackageManager pm = mContext.getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            if (uid == 0) {
                drainType = DrainType.KERNEL;
            } else if ("mediaserver".equals(name)) {
                drainType = DrainType.MEDIASERVER;
            }
            return;
        }

        getNameIcon();
    }

    /**
     * Sets name and icon
     */
    private void getNameIcon() {
        PackageManager pm = mContext.getPackageManager();
        final int uid = uidObj.getUid();
        final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            name = Integer.toString(uid);
            return;
        }

        String[] packageLabels = new String[packages.length];
        System.arraycopy(packages, 0, packageLabels, 0, packages.length);

        for (int i = 0; i < packageLabels.length; i++) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(packageLabels[i], 0);
                CharSequence label = ai.loadLabel(pm);
                if (label != null) {
                    packageLabels[i] = label.toString();
                }
                if (ai.icon != 0) {
                    defaultPackageName = packages[i];
                    icon = ai.loadIcon(pm);
                    break;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (icon == null)
            icon = defaultActivityIcon;

        if (packageLabels.length == 1) {
            name = packageLabels[0];
        } else {
            for (String pkgName : packages) {
                try {
                    final PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                    if (pi.sharedUserLabel != 0) {
                        final CharSequence nm = pm.getText(pkgName, pi.sharedUserLabel, pi.applicationInfo);
                        if (nm != null) {
                            name = nm.toString();
                            if (pi.applicationInfo.icon != 0) {
                                defaultPackageName = pkgName;
                                //YLog.e("packagename", defaultPackageName);
                                icon = pi.applicationInfo.loadIcon(pm);
                            }
                            break;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        final String uidString = Integer.toString(uidObj.getUid());
        UidToDetail utd = new UidToDetail();
        utd.name = name;
        utd.icon = icon;
        utd.packageName = defaultPackageName;
        mUidCache.put(uidString, utd);
    }

    @Override
    public String toString() {
        return "BatterySipper [name=" + name + ", icon=" + icon + ", value=" + value + ", tcpBytesReceived="
                + tcpBytesReceived + ", tcpBytesSent=" + tcpBytesSent + ", percent=" + percent + ", pkgName="
                + pkgName + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        bitmap = DrawableToString.drawableToByte(icon);
        dest.writeString(bitmap);
        dest.writeString(pkgName);
        dest.writeDouble(value);
        dest.writeLong(tcpBytesReceived);
        dest.writeLong(tcpBytesSent);
        dest.writeDouble(percent);
    }

    public BattRankInfo(Parcel source) {
        name = source.readString();
        bitmap = source.readString();
        pkgName = source.readString();
        value = source.readDouble();
        tcpBytesReceived = source.readLong();
        tcpBytesSent = source.readLong();
        percent = source.readDouble();
    }

    public static final Parcelable.Creator<BattRankInfo> CREATOR = new Parcelable.Creator<BattRankInfo>() {

        public BattRankInfo createFromParcel(Parcel source) {

            return new BattRankInfo(source);
        }

        public BattRankInfo[] newArray(int size) {

            return new BattRankInfo[size];

        }

    };
}