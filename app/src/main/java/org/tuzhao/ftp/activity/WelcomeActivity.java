package org.tuzhao.ftp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.util.Umeng;
import org.tuzhao.ftp.util.PermissionActivityUtil;
import org.tuzhao.ftp.util.WeakRunnable;

import java.util.ArrayList;

import be.ppareit.swiftp.FsService;
import be.ppareit.swiftp.FsSettings;
import be.ppareit.swiftp.gui.MainActivity;

public class WelcomeActivity extends Activity {

    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean auto = (FsSettings.isNoDisplayStart() && autoStart());
        if (auto) {
            setTheme(R.style.NoDisplay);
        } else {
            setTheme(R.style.AppThemeFullScreen);
        }
        super.onCreate(savedInstanceState);

        MobclickAgent.setDebugMode(true);
        MobclickAgent.enableEncrypt(true);

        String deviceInfo = Umeng.getDeviceInfo(this);
        Log.i(TAG, "device info: " + deviceInfo);

        if (auto) {
            if (FsService.isRunning()) {
                Toast.makeText(this, R.string.auto_note, Toast.LENGTH_LONG).show();
            } else {
                Intent start = new Intent(FsService.ACTION_START_FTPSERVER);
                start.setPackage(getPackageName());
                sendBroadcast(start);
            }
            finish();
        } else {
            setContentView(R.layout.activity_welcome);
            new android.os.Handler().postDelayed(new WelcomeRunnable(this), 3000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private boolean autoStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final ArrayList<String> deniedList = getPermissionUtil().getDeniedPermissionList();
            if (null != deniedList && deniedList.size() > 0) {
                return false;
            }
        }
        WifiManager wifiManager = (WifiManager)
                                      this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (null == wifiManager) {
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo != null && FsSettings.isAutoConnectWifi(wifiInfo.getSSID());
    }

    private PermissionActivityUtil getPermissionUtil() {
        return new PermissionActivityUtil(this);
    }

    private static class WelcomeRunnable extends WeakRunnable<WelcomeActivity> {

        WelcomeRunnable(WelcomeActivity context) {
            super(context);
        }

        @Override
        public void weakRun(WelcomeActivity context) {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
            context.finish();
        }
    }

}
