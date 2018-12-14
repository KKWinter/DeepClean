package com.catchgift.trashclear.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.URLUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tommyduan on 15/6/10. project in ST_ADS
 */
public class Utils {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    /**
     * @param ctx Context
     */
    public static void init(Context ctx) {
        mContext = ctx;
    }


    /**
     * 获取Resource对象
     * @return
     */
    public static Resources getResources(){
        return GlobalApplication.getContext().getResources();
    }

    /**
     * 获取字符串的资源
     * @param resId
     * @return
     */
    public static String getString(int resId){
        return getResources().getString(resId);
    }

    /**
     * 获取图片资源
     * @param resId
     * @return
     */
    public static Drawable getDrawable(int resId){
        return getResources().getDrawable(resId);
    }

    /**
     * 获取颜色资源
     * @param resId
     * @return
     */
    public static int getColor(int resId){
        return getResources().getColor(resId);
    }





    public static int getIdByName(Context context, String className, String name) {
        String defPackage = context.getPackageName();
        return context.getResources()
                .getIdentifier(name, className, defPackage);
    }



    /**
     * @return AndroidID
     */
    public static String getAndroidid() {
        String androidid = "";
        try {
            androidid = Secure.getString(getContext().getContentResolver(),
                    Secure.ANDROID_ID);
        } catch (Exception e) {
            YLog.e("getAndroidid Error");
        }
        return androidid;
    }

    public static String getDeviceSerial() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        return serial;
    }

    /**
     * @return Androidid MD5
     */
    public static String getAndroididWithMD5() {
        String androidid = "";
        try {
            androidid = Secure.getString(getContext().getContentResolver(),
                    Secure.ANDROID_ID);
            if (androidid != null) {
                androidid = MD5(androidid);
            }
        } catch (Exception e) {
            YLog.e("getAndroididWithMD5 Error");
        }
        return androidid;
    }

    /**
     * @return MAC
     */
    public static String getRouteMac() {
        try {
            WifiManager wm = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wm.getConnectionInfo();
            if (info.getBSSID() == null) {
                return "";
            } else {
                return info.getBSSID() + "";
            }
        } catch (Exception e) {
            YLog.e("getRouteMac Error=" + e.getMessage());
            return "";
        }
    }

    /**
     * @return SSID
     */
    public static String getRouteSSID() {
        try {
            WifiManager wm = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wm.getConnectionInfo();
            if (info.getSSID().contains("<")) {
                return "";
            } else {
                return info.getSSID().replace("\"", "") + "";
            }
        } catch (Exception e) {
            YLog.e("getRouteSSID Error=" + e.getMessage());
            return "";
        }
    }

    /**
     * @return UUID
     */
    public static UUID getRoundShareID() {
        return UUID.randomUUID();
    }

    public static String getAppPackageName() {
        String pn = "";
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    mContext.getPackageName(), 0);
            pn = info.packageName;
        } catch (Exception e) {
            YLog.e("getAppPackageName Error=" + e.getMessage());
            return pn;
        }
        return pn;
    }

    public static String getScreenOrientation() {
        String screenOrientation = "";

        int o = mContext.getResources().getConfiguration().orientation;
        screenOrientation = o + "";

        return screenOrientation;
    }

    public static String getAppname() {
        String appname = "";
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    mContext.getPackageName(), 0);
            appname = info.applicationInfo.loadLabel(manager).toString();
        } catch (Exception e) {
            YLog.e("getAppname Error=" + e.getMessage());
            return appname;
        }
        return appname;
    }

    public static String getAppVersion() {
        String ver = "";
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(
                    mContext.getPackageName(), 0);
            ver = info.versionName;
        } catch (Exception e) {
            YLog.e("getAppVersion Error=" + e.getMessage());
            return ver;
        }
        return ver;
    }

    public static String getMac() {
        String mac = "";
        try {
            WifiManager wifi = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String macAddress = info.getMacAddress();
            if (macAddress != null) {
                mac = macAddress;
            }
        } catch (Exception e) {
            YLog.e("getMac Error=" + e.getMessage());
        }
        return mac;
    }

    public static String getMacWhitMD5() {
        String mac = "";
        try {
            WifiManager wifi = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String macAddress = info.getMacAddress();
            if (macAddress != null) {
                if (isNotEmpty(macAddress)) {
                    mac = MD5(macAddress);
                }
            }

        } catch (Exception e) {
            YLog.e("getMacWhitMD5 Error=" + e.getMessage());
        }
        return mac;
    }

    public static String getSysteminfo() {
        String systemInfo = "";
        try {
            systemInfo = Build.VERSION.RELEASE;
        } catch (Exception e) {
            YLog.e("getSysteminfo Error=" + e.getMessage());
            return systemInfo;
        }
        return systemInfo;
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isNotEmpty(String str) {
        if (str != null && !"".equals(str)) {
            return true;
        }
        return false;
    }

    public static long getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static boolean isNetEnable() {
        try {
            ConnectivityManager manger = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manger.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        } catch (Exception e) {
            YLog.e("isNetEnable Error=" + e.getMessage());
            return false;
        }
    }

    public static boolean isWifiConnected() {
        ConnectivityManager mConnectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        State state_wifi = null;
        state_wifi = mConnectivity
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (null != state_wifi && State.CONNECTED == state_wifi) {
            return true;
        }
        return false;
    }

    public static String getIMEI() {
        String imei = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                imei = telephonyManager.getDeviceId();
                if (TextUtils.isEmpty(imei)) {
                    imei = Secure.getString(mContext.getContentResolver(),
                            Secure.ANDROID_ID);
                }
            }
        } catch (Exception e) {
            YLog.e("getIMEI Error=" + e.getMessage());
        }
        return imei;
    }

    public static String getIMEIWhitMD5() {
        String imei = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                imei = telephonyManager.getDeviceId();
                if (TextUtils.isEmpty(imei)) {
                    imei = Secure.getString(mContext.getContentResolver(),
                            Secure.ANDROID_ID);
                    if (imei != null) {
                        imei = MD5(imei);
                    }
                } else {
                    imei = MD5(imei);
                }
            }
        } catch (Exception e) {
            YLog.e("getIMEIWhitMD5 Error=" + e.getMessage());
        }
        return imei;
    }

    public static String getIMSI() {
        String imsi = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                imsi = telephonyManager.getSubscriberId();
            }
            if (TextUtils.isEmpty(imsi)) {
                imsi = "UNKNOWN";
            }
        } catch (Exception e) {
            YLog.e("getIMSI Error=" + e.getMessage());
        }
        return imsi;
    }

    public static String getIMSIWhitMD5() {
        String imsi = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                imsi = telephonyManager.getSubscriberId();
                if (imsi != null) {
                    imsi = MD5(imsi);
                }
            }
            if (TextUtils.isEmpty(imsi)) {
                imsi = "UNKNOWN";
            }
        } catch (Exception e) {
            YLog.e("getIMSIWhitMD5 Error=" + e.getMessage());
        }
        return imsi;
    }

    public static String MD5(String fileName) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = fileName.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            YLog.e("MD5 Error=" + e.getMessage());
            return null;
        }
    }

    public static String getBrand() {
        String brand = Build.BRAND;
        if (brand == null) {
            brand = "";
        }
        return brand;
    }

    public static String getProductModel() {
        String pn = "";
        try {
            pn = Build.MODEL;
        } catch (Exception e) {
            YLog.e("getProductModel Error=" + e.getMessage());
            return pn;
        }
        return pn;
    }

    @TargetApi(8)
    public static int getLocalAreaCode() {
        try {
            TelephonyManager telephony = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);

            switch (telephony.getPhoneType()) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) telephony
                            .getCellLocation();
                    if (gsmCellLocation != null) {
                        return gsmCellLocation.getLac();
                    }
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) telephony
                            .getCellLocation();
                    if (cdmaCellLocation != null) {
                        return cdmaCellLocation.getBaseStationId();
                    }
                    break;
            }
        } catch (Exception e) {
            YLog.e("getLocalAreaCode Error=" + e.getMessage());
        }
        return -1;
    }

    public static boolean isSDCardEnable() {
        String state = android.os.Environment.getExternalStorageState();
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
            if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
                return true;
            }
        }
        return false;
    }

    public static String getNetworkOperator() {
        String operator = "";
        try {
            TelephonyManager manager = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (manager.getNetworkOperator() != null) {
                operator = manager.getNetworkOperator();
                return operator;
            }
        } catch (Exception e) {
            YLog.e("getNetworkOperator Error:" + e.getMessage());
        }
        return "";
    }

    public static Location getLocation() {
        try {
            LocationManager locationManager;
            String contextString = Context.LOCATION_SERVICE;
            locationManager = (LocationManager) mContext
                    .getSystemService(contextString);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider == null) {
                return null;
            }
//			Location location = locationManager.getLastKnownLocation(provider);
//			if (location == null) {
//				return null;
//			}
//			return location;
        } catch (Exception e) {
            YLog.e("getLocation Error=" + e.getMessage());
        }
        return null;
    }

    public static String getDeviceScreenSizeWithString(Boolean isWidth) {
        try {
            DisplayMetrics displayMetrics = mContext.getResources()
                    .getDisplayMetrics();
            int widthPixels = displayMetrics.widthPixels;
            int heightPixels = displayMetrics.heightPixels;
            if (isWidth) {
                return widthPixels + "";
            } else {
                return heightPixels + "";
            }
        } catch (Exception e) {
            YLog.e("getDeviceScreenSizeWithString Error=" + e.getMessage());
        }
        return "";
    }

    public static int[] getDeviceScreenSizeWithInt() {
        DisplayMetrics displayMetrics = mContext.getResources()
                .getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        int[] num = {widthPixels, heightPixels};
        return num;
    }

    public static double getDeviceDensity() {
        try {
            DisplayMetrics displayMetrics = mContext.getResources()
                    .getDisplayMetrics();
            double density = displayMetrics.density;
            return density;
        } catch (Exception e) {
            YLog.e("getDeviceDensity Error=" + e.getMessage());
        }
        return -1;
    }

    public static int IntegerRounded(float i) {
        int roundedIntefer;
        try {
            roundedIntefer = Integer.valueOf(new BigDecimal(i).setScale(0,
                    BigDecimal.ROUND_HALF_UP).toString());
            return roundedIntefer;
        } catch (NumberFormatException e) {
            YLog.e("IntegerRounded Error=" + e.getMessage());
        }
        return 0;
    }

    public static boolean isPicture(String str) {
        if (isNotEmpty(str)) {
            String suffix = str.substring(str.lastIndexOf(".") + 1,
                    str.length());
            if (suffix != null) {
                if (suffix.equalsIgnoreCase("png")
                        || suffix.equalsIgnoreCase("jpeg")
                        || suffix.equalsIgnoreCase("jpg")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getCurrentNetWorkInfo() {
        String netInfo = "";
        try {
            ConnectivityManager connectionManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                netInfo = "0";
            } else {
                netInfo = networkInfo.getExtraInfo();
            }
            return netInfo;
        } catch (Exception e) {
            YLog.e("getCurrentNetWorkInfo Error=" + e.getMessage());
        }
        return netInfo;
    }

    public static String GenerateJPGName(String url) {
        try {
            if (isNotEmpty(url)) {
                String suffixName = url.substring(url.lastIndexOf("."));
                String md5Vid = Utils.MD5(url);
                return md5Vid + suffixName;
            }
        } catch (Exception e) {
            YLog.e("GenerateJPGName Error=" + e.getMessage());
        }
        return "";
    }

    public static File imageDataDir() {
        File dir = new File(mContext.getFilesDir().getPath() + "/mvad/image/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static boolean isApk(String url) {
        if (isNotEmpty(url) && URLUtil.isHttpUrl(url)) {
            String localName = url.substring(url.lastIndexOf("/") + 1);
            if (localName != null && "apk".equalsIgnoreCase(localName)) {
                return true;
            }
        }
        return false;
    }

    public static String filterImgSrc(String html) {
        Pattern patternImgStr = Pattern.compile(
                "<\\s*img\\s*(?:[^>]*)src\\s*=\\s*([^>]+)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = patternImgStr.matcher(html);
        String returnString = null;
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group == null) {
                continue;
            }
            if (group.startsWith("'")) {
                returnString = group.substring(1, group.indexOf("'", 1));
            } else if (group.startsWith("\"")) {
                returnString = group.substring(1, group.indexOf("\"", 1));
            } else {
                returnString = group.split("\\s")[0];
            }
        }
        return returnString;
    }

    public static boolean isAction(final Context context) {
        try {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            @SuppressWarnings("deprecation")
            List<RunningTaskInfo> infos = am.getRunningTasks(1);
            if (context.getClass().getCanonicalName()
                    .equals(infos.get(0).topActivity.getClassName())) {
                return true;
            }
        } catch (Exception e) {
            YLog.e(e.getMessage());
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(8)
    public static Boolean getScreenState(Context context) {
        try {
            PowerManager manager = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            KeyguardManager mKeyguardManager = (KeyguardManager) context
                    .getSystemService(Context.KEYGUARD_SERVICE);
            if (manager.isScreenOn()) {
                return !mKeyguardManager.inKeyguardRestrictedInputMode();
            }
        } catch (Exception e) {
            YLog.e(e.getMessage());
        }
        return false;
    }

    public static String getMD5(String val) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(val.getBytes());
        byte[] m = md5.digest();
        return getString(m);
    }

    /**
     * @param b byte
     * @return String
     */
    private static String getString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(b[i]);
        }
        return sb.toString();
    }

    public static String getTextFromAssetsFile(Context context, String filename) {
        if (context == null) {
            context = mContext;
        }

        InputStream is;

        Writer writer = new StringWriter();
        char[] buffer = new char[8 * 1024];
        try {
            is = context.getResources().getAssets().open(filename);
            Reader reader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
            int n = 0;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        return writer.toString();
    }

    public static boolean isHttpUrl(String httpurl) {
        if (httpurl != null && !"".equals(httpurl)
                && httpurl.startsWith("http://")) {
            return true;
        }
        return false;
    }

    public static byte[] bmpToByteArray(Bitmap bitmap, boolean paramBoolean) {
        Bitmap localBitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888);
        Canvas localCanvas = new Canvas(localBitmap);
        int i;
        int j;
        if (bitmap.getHeight() > bitmap.getWidth()) {
            i = bitmap.getWidth();
            j = bitmap.getWidth();
        } else {
            i = bitmap.getHeight();
            j = bitmap.getHeight();
        }
        while (true) {
            localCanvas.drawBitmap(bitmap, new Rect(0, 0, i, j), new Rect(0, 0, 80, 80), null);
            if (paramBoolean)
                bitmap.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
                e.printStackTrace();
            }
            i = bitmap.getHeight();
            j = bitmap.getHeight();
        }
    }

}
