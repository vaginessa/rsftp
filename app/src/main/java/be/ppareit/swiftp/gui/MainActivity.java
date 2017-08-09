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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

import org.tuzhao.ftp.BuildConfig;
import org.tuzhao.ftp.R;
import org.tuzhao.ftp.activity.BaseActivity;
import org.tuzhao.ftp.activity.PermissionActivity;
import org.tuzhao.ftp.util.PermissionUtil;
import org.tuzhao.ftp.util.System;
import org.tuzhao.ftp.util.WeakRunnable;

import be.ppareit.swiftp.FsService;
import be.ppareit.swiftp.FsSettings;

/**
 * This is the main activity for RsFTP, it enables the user to start the server service
 * and allows the users to change the settings.
 */
public class MainActivity extends BaseActivity {

    private boolean isNeedCheckAgain;

    private PermissionUtil permissionUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        log("onCreate()");
        setTheme(FsSettings.getTheme());
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new PreferenceFragment())
            .commit();

        permissionCheck();
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart()");
        if (isNeedCheckAgain) {
            permissionCheck();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String[] deniedStrings = permissionUtil.onRequestPermissionsResult(requestCode,
            permissions, grantResults);
        showPermissionDialog(deniedStrings);
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
                             .setPositiveButton(R.string.submit, (dialogInterface, i) -> appExit())
                             .setNegativeButton(R.string.cancel, null)
                             .create();
        ad.show();
    }

    private void permissionCheck() {
        permissionUtil = new PermissionUtil(this);
        permissionUtil.init();
        permissionUtil.request();
    }

    private void showPermissionDialog(final String[] deniedStrings) {
        if (deniedStrings.length > 0) {
            final Context context = getActivity();
            StringBuilder builder = new StringBuilder();
            if (System.isZh(this)) {
                builder.append("这里仍然有");
                builder.append(deniedStrings.length);
                builder.append("条权限被拒绝");
            } else {
                builder.append("there");
                if (deniedStrings.length > 1) {
                    builder.append(" are ");
                } else {
                    builder.append(" is ");
                }
                builder.append("still ");
                builder.append(deniedStrings.length);
                if (deniedStrings.length > 1) {
                    builder.append(" permissions ");
                } else {
                    builder.append(" permission ");
                }
                builder.append("denied");
            }
            String msg = String.format(context.getString(R.string.permission_msg), builder.toString());

            AlertDialog ad = new AlertDialog.Builder(context)
                                 .setTitle(R.string.permission_note)
                                 .setMessage(msg)
                                 .setCancelable(false)
                                 .setPositiveButton(R.string.permission_bt_set, (dialog, i) -> {
                                     isNeedCheckAgain = true;
                                     dialog.dismiss();
                                     appSetting();
                                 })
                                 .setNeutralButton(R.string.permission_bt_detail, (dialog, i) -> {
                                     isNeedCheckAgain = true;
                                     dialog.dismiss();
                                     Intent intent = new Intent(getActivity(), PermissionActivity.class);
                                     getActivity().startActivity(intent);
                                 })
                                 .setNegativeButton(R.string.permission_bt_exit, (dialog, i) -> {
                                     isNeedCheckAgain = false;
                                     dialog.dismiss();
                                     appExit();
                                 })
                                 .create();
            ad.show();
        } else {
            isNeedCheckAgain = false;
        }
    }

    private void appExit() {
        MobclickAgent.onKillProcess(this);
        Intent intent = new Intent(FsService.ACTION_STOP_FTPSERVER);
        intent.setPackage(getActivity().getPackageName());
        getActivity().sendBroadcast(intent);
        getActivity().finish();
        new Handler().postDelayed(new ExitRunnable(getActivity()), 1000);
    }

    private void appSetting() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        if (System.isIntentAvailable(this, intent)) {
            startActivity(intent);
        } else {
            showMsg(getString(R.string.open_error_setting));
        }
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
