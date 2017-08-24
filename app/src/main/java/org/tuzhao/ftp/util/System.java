package org.tuzhao.ftp.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;
import org.tuzhao.ftp.entity.RsFTPFile;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.RsLocalFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * zhaotu
 * 17-8-4
 */
public final class System {

    public static final String ACTION_SERVER_CURRENT_PATH = "action_server_current_path";
    public static final String ACTION_SERVER_EXCEPTION_CONNECT = "action_server_connect_exception";
    public static final String ACTION_SERVER_EXCEPTION_LOGIN = "action_server_login_exception";
    public static final String ACTION_SERVER_FAILED_LOGIN = "action_server_failed_login";

    private static final String EXTRA_CURRENT_PATH = "extra_current_path";
    private static final String EXTRA_ERROR_CONNECT_MSG = "extra_error_msg";
    private static final String EXTRA_ERROR_LOGIN_MSG = "extra_login_msg";

    private static final String TAG = "System";

    public static void sendServerCurrentPathBroadcast(Context context, String path) {
        Intent intent = new Intent(ACTION_SERVER_CURRENT_PATH);
        intent.putExtra(EXTRA_CURRENT_PATH, path);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendServerConnectExceptionBroadcast(Context context, String errorMsg) {
        Intent intent = new Intent(ACTION_SERVER_EXCEPTION_CONNECT);
        intent.putExtra(EXTRA_ERROR_CONNECT_MSG, errorMsg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendServerLoginExceptionBroadcast(Context context, String errorMsg) {
        Intent intent = new Intent(ACTION_SERVER_EXCEPTION_LOGIN);
        intent.putExtra(EXTRA_ERROR_LOGIN_MSG, errorMsg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendServerLoginFailed(Context context) {
        Intent intent = new Intent(ACTION_SERVER_FAILED_LOGIN);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static String getServerCurrentPath(Intent intent) {
        String path = null;
        if (null != intent) {
            path = intent.getStringExtra(EXTRA_CURRENT_PATH);
        }
        return path;
    }

    public static String getServerErrorConnectMsg(Intent intent) {
        String msg = null;
        if (null != intent) {
            msg = intent.getStringExtra(EXTRA_ERROR_CONNECT_MSG);
        }
        return msg;
    }

    public static String getServerErrorLoginMsg(Intent intent) {
        String msg = null;
        if (null != intent) {
            msg = intent.getStringExtra(EXTRA_ERROR_LOGIN_MSG);
        }
        return msg;
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

    public static ArrayList<RsFile> convertFTPFileToRsFile(ArrayList<FTPFile> srcList) {
        ArrayList<RsFile> files = new ArrayList<>();
        for (int i = 0; i < srcList.size(); i++) {
            FTPFile ftpFile = srcList.get(i);
            files.add(new RsFTPFile(ftpFile));
        }
        return files;
    }

    public static ArrayList<RsFile> convertFileToRsFile(File[] srcList) {
        ArrayList<RsFile> files = new ArrayList<>();
        if (null != srcList) {
            for (File file : srcList) {
                files.add(new RsLocalFile(file));
            }
        }
        return files;
    }

    private static void log(String msg) {
        Log.i(TAG, msg);
    }

}
