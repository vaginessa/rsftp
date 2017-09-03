package org.tuzhao.ftp.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * zhaotu
 * 17-8-4
 */
public final class System {

    private static final String TAG = "System";

    public static final String SHARED_CONFIG_FILE = "config";
    public static final String SHARED_WIFI_KEY = "wifi_select";

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        );
        for (ResolveInfo info : list) {
            String name = info.activityInfo.packageName;
            log("resolve package: " + name);
        }
        return list.size() > 0;
    }

    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        log("language locale: " + language);
        return language.endsWith("zh");
    }

    public static void threadInfo() {
        Thread thread = Thread.currentThread();
        long id = thread.getId();
        String name = thread.getName();
        int priority = thread.getPriority();
        ThreadGroup group = thread.getThreadGroup();
        log("thread info[id: " + id + " name: " + name + " priority: " + priority + " group: " + group + " ]");
    }

    public static boolean isAndroidO() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
    }

    public static String listToString(ArrayList<String> list) {
        String str = "";
        try {
            ByteArrayOutputStream outByte = new ByteArrayOutputStream();
            ObjectOutputStream outObject = new ObjectOutputStream(outByte);
            outObject.writeObject(list);
            str = new String(Base64.encode(outByte.toByteArray(), Base64.DEFAULT));
            outObject.close();
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<String> stringToList(String string) {
        final ArrayList<String> list = new ArrayList<>();
        try {
            if (null != string && !TextUtils.isEmpty(string)) {
                byte[] bytes = Base64.decode(string.getBytes(), Base64.DEFAULT);
                ByteArrayInputStream inByte = new ByteArrayInputStream(bytes);
                ObjectInputStream inObj = new ObjectInputStream(inByte);
                ArrayList<String> read = (ArrayList<String>) inObj.readObject();
                list.addAll(read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void log(String msg) {
        Log.i(TAG, msg);
    }

}
