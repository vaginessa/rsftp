/*******************************************************************************
 * Copyright (c) 2012-2013 Pieter Pareit.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors:
 * Pieter Pareit - initial API and implementation
 ******************************************************************************/

package be.ppareit.swiftp.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

import org.tuzhao.ftp.BuildConfig;
import org.tuzhao.ftp.R;
import org.tuzhao.ftp.activity.BaseActivity;
import org.tuzhao.ftp.util.System;
import org.tuzhao.ftp.util.WeakRunnable;

import be.ppareit.swiftp.FsService;
import be.ppareit.swiftp.FsSettings;
import be.ppareit.swiftp.WifiStateChangeReceiver;

/**
 * This is the main activity for RsFTP, it enables the user to start the server service
 * and allows the users to change the settings.
 */
public class MainActivity extends BaseActivity {

    private WifiStateChangeReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        log("onCreate()");
        setTheme(FsSettings.getTheme());
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new PreferenceFragment())
            .commit();

        receiver = new WifiStateChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getApplicationContext().registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != receiver)
            getApplicationContext().unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_feedback) {
            String to = "tuzhaocn@gmail.com";
            String subject = "FTP Server feedback";
            String message = "Device: " + Build.MODEL + "\n" +
                                 "Android version: " + VERSION.RELEASE + "-" + VERSION.SDK_INT + "\n" +
                                 "Application: " + BuildConfig.APPLICATION_ID + " (" + BuildConfig.FLAVOR + ")\n" +
                                 "Application version: " + BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE + "\n" +
                                 "Feedback: \n_";

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
            email.putExtra(Intent.EXTRA_SUBJECT, subject);
            email.putExtra(Intent.EXTRA_TEXT, message);
            email.setType("message/rfc822");
            if (System.isIntentAvailable(this, email)) {
                startActivity(email);
            } else {
                showMsg(getString(R.string.open_error_mail));
            }
        } else if (item.getItemId() == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog ad = new AlertDialog.Builder(getActivity())
                             .setTitle(R.string.exit_title)
                             .setMessage(R.string.exit_msg)
                             .setPositiveButton(R.string.submit, (dialogInterface, i) -> appExit(getActivity()))
                             .setNegativeButton(R.string.cancel, null)
                             .create();
        ad.show();
    }

    public static void appExit(Activity context) {
        MobclickAgent.onKillProcess(context);
        Intent intent = new Intent(FsService.ACTION_STOP_FTPSERVER);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
        context.finish();
        new Handler().postDelayed(new ExitRunnable(context), 1000);
    }

    private static class ExitRunnable extends WeakRunnable<Activity> {

        ExitRunnable(Activity context) {
            super(context);
        }

        @Override
        public void weakRun(Activity context) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

}
