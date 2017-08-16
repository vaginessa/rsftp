package org.tuzhao.ftp.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.Locale;

/**
 * zhaotu
 * 17-8-4
 */
public final class System {

    public static final String ACTION_SERVER_CURRENT_PATH = "action_server_current_path";
    private static final String EXTRA_CURRENT_PATH = "extra_current_path";

    private static final String TAG = "System";

    public static void sendServerCurrentPathBroadcast(Context context, String path) {
        Intent intent = new Intent(ACTION_SERVER_CURRENT_PATH);
        intent.putExtra(EXTRA_CURRENT_PATH, path);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static String getServerCurrentPath(Intent intent) {
        String path = null;
        if (null != intent) {
            path = intent.getStringExtra(EXTRA_CURRENT_PATH);
        }
        return path;
    }

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

    private static void log(String msg) {
        Log.i(TAG, msg);
    }

}
