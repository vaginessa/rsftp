package org.tuzhao.ftp.util;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * zhaotu
 * 17-8-4
 */
public final class PermissionUtil {

    private static final String TAG = "PermissionUtil";
    private final static int PERMISSIONS_REQUEST_CODE = 12;
    private final ArrayList<String> list = new ArrayList<>();
    private final Activity context;

    public PermissionUtil(Activity context) {
        this.context = context;
    }

    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
                String[] permissions = pi.requestedPermissions;
                if (permissions != null) {
                    for (String item : permissions) {
                        log("denied permission: " + item);
                        if (ContextCompat.checkSelfPermission(context, item) != PackageManager.PERMISSION_GRANTED) {
                            list.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void request() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (list.size() > 0) {
                String[] strings = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    String permission = list.get(i);
                    strings[i] = permission;
                }
                ActivityCompat.requestPermissions(context, strings, PERMISSIONS_REQUEST_CODE);
                list.clear();
            }
        }
    }

    public String[] onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final ArrayList<String> list = new ArrayList<>();
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            log("permissions: " + Arrays.toString(permissions));
            log("grantResults: " + Arrays.toString(grantResults));
            if ((null != permissions && null != grantResults) && (permissions.length == grantResults.length)) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        list.add(permissions[i]);
                    }
                }
            }
        }
        String[] array = new String[]{};
        String[] strings = list.toArray(array);
        log("denied permissions: " + Arrays.toString(strings));
        return strings;
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }
}
