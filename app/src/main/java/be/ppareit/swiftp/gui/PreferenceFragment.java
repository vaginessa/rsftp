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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import net.vrallev.android.cat.Cat;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.activity.PermissionActivity;
import org.tuzhao.ftp.fragment.DonationDialogFragment;
import org.tuzhao.ftp.fragment.WifiDialogFragment;
import org.tuzhao.ftp.util.PermissionFragmentUtil;
import org.tuzhao.ftp.util.System;
import org.tuzhao.ftp.util.Umeng;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import be.ppareit.swiftp.App;
import be.ppareit.swiftp.FsService;
import be.ppareit.swiftp.FsSettings;

/**
 * This is the main activity for swiftp, it enables the user to start the server service
 * and allows the users to change the settings.
 */
public class PreferenceFragment extends android.preference.PreferenceFragment implements
    OnSharedPreferenceChangeListener {

    private static final String TAG = "PreferenceFragment";
    private static final int MSG_FAIL_START = 0x10;

    private Handler mHandler;
    private PermissionFragmentUtil permissionUtil;

    private EditTextPreference mPassWordPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createHandler();
        addPreferencesFromResource(R.xml.preferences);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Resources resources = getResources();

        TwoStatePreference runningPref = findPref("running_switch");
        updateRunningState();
        runningPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                uEvent(Umeng.EVENT_01);
                startServer();
            } else {
                uEvent(Umeng.EVENT_02);
                stopServer();
            }
            return true;
        });

        PreferenceScreen prefScreen = findPref("preference_screen");
        Preference marketVersionPref = findPref("donation");
        marketVersionPref.setOnPreferenceClickListener(preference -> {
            DonationDialogFragment.show(getActivity());
            return true;
        });

        updateLoginInfo();

        EditTextPreference usernamePref = findPref("username");
        usernamePref.setOnPreferenceChangeListener((preference, newValue) -> {
            String newUsername = (String) newValue;
            if (preference.getSummary().equals(newUsername))
                return false;
            if (!newUsername.matches("[a-zA-Z0-9]+")) {
                Toast.makeText(getActivity(),
                    R.string.username_validation_error, Toast.LENGTH_LONG).show();
                return false;
            }
            uEvent(Umeng.EVENT_03);
            stopServer();
            return true;
        });

        mPassWordPref = findPref("password");
        mPassWordPref.setOnPreferenceChangeListener((preference, newValue) -> {
            uEvent(Umeng.EVENT_04);
            stopServer();
            return true;
        });
        Preference connect = findPref("autoconnect_preference");
        connect.setOnPreferenceClickListener(preference -> {
            WifiDialogFragment.show(getActivity());
            return true;
        });

        EditTextPreference portnum_pref = findPref("portNum");
        portnum_pref.setSummary(sp.getString("portNum",
            resources.getString(R.string.portnumber_default)));
        portnum_pref.setOnPreferenceChangeListener((preference, newValue) -> {
            String newPortnumString = (String) newValue;
            if (preference.getSummary().equals(newPortnumString))
                return false;
            int portnum = 0;
            try {
                portnum = Integer.parseInt(newPortnumString);
            } catch (Exception e) {
                Cat.d("Error parsing port number! Moving on...");
            }
            if (portnum <= 0 || 65535 < portnum) {
                Toast.makeText(getActivity(),
                    R.string.port_validation_error, Toast.LENGTH_LONG).show();
                return false;
            }
            preference.setSummary(newPortnumString);
            uEvent(Umeng.EVENT_05);
            stopServer();
            return true;
        });

        final EditTextPreference waitTimePref = findPref("waitTime");
        String timeSave = sp.getString("waitTime", resources.getString(R.string.disconnect_wifi_wait_default));
        waitTimePref.setSummary(String.valueOf(FsSettings.getDisconnectTime(timeSave)));
        waitTimePref.setOnPreferenceChangeListener((preference, value) -> {
            final String input = String.valueOf(value);
            if (preference.getSummary().equals(input)) {
                return false;
            }
            int time;
            try {
                time = Integer.parseInt(input);
            } catch (Exception e) {
                showMsg(getString(R.string.disconnect_wifi_input_error));
                return false;
            }
            if (time < 0 || time > 60) {
                showMsg(getString(R.string.disconnect_wifi_input_error));
                return false;
            }
            preference.setSummary(String.valueOf(time));
            return true;
        });
        waitTimePref.setOnPreferenceClickListener(preference -> {
            waitTimePref.getEditText().setText(waitTimePref.getSummary().toString());
            return false;
        });

        Preference chroot_pref = findPref("chrootDir");
        chroot_pref.setSummary(FsSettings.getChrootDirAsString());
        chroot_pref.setOnPreferenceClickListener(preference -> {
            AlertDialog folderPicker = new FolderPickerDialogBuilder(getActivity(), FsSettings.getChrootDir())
                                           .setSelectedButton(R.string.select, path -> {
                                               if (preference.getSummary().equals(path))
                                                   return;
                                               if (!FsSettings.setChrootDir(path))
                                                   return;
                                               // TODO: this is a hotfix, create correct resources, improve UI/UX
                                               final File root = new File(path);
                                               if (!root.canRead()) {
                                                   Toast.makeText(getActivity(),
                                                       "Notice that we can't read/write in this folder.",
                                                       Toast.LENGTH_LONG).show();
                                               } else if (!root.canWrite()) {
                                                   Toast.makeText(getActivity(),
                                                       "Notice that we can't write in this folder, reading will work. Writing in subfolders might work.",
                                                       Toast.LENGTH_LONG).show();
                                               }

                                               preference.setSummary(path);
                                               uEvent(Umeng.EVENT_06);
                                               stopServer();
                                           })
                                           .setNegativeButton(R.string.cancel, null)
                                           .create();
            folderPicker.show();
            return true;
        });

        final CheckBoxPreference wakelock_pref = findPref("stayAwake");
        wakelock_pref.setOnPreferenceChangeListener((preference, newValue) -> {
            stopServer();
            return true;
        });

        ListPreference theme = findPref("theme");
        theme.setSummary(theme.getEntry());
        theme.setOnPreferenceChangeListener((preference, newValue) -> {
            uEvent(Umeng.EVENT_07);
            theme.setSummary(theme.getEntry());
            getActivity().recreate();
            return true;
        });

        Preference permission = findPref("permission");
        permission.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), PermissionActivity.class));
            return true;
        });

        Preference evaluate = findPref("evaluate");
        evaluate.setOnPreferenceClickListener(preference -> {
            try {
                final Activity context = getActivity();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("market://details?id=" + context.getPackageName()));
                List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
                if (null != list && list.size() > 0) {
                    startActivity(intent);
                } else {
                    showMsg(getString(R.string.evaluate_note));
                }
            } catch (Exception e) {
                showMsg(getString(R.string.evaluate_failure));
                e.printStackTrace();
            }
            return true;
        });

        Preference help = findPref("help");
        help.setOnPreferenceClickListener(preference -> {
            Cat.v("On preference help clicked");
            Context context = getActivity();
            AlertDialog ad = new AlertDialog.Builder(context)
                                 .setTitle(R.string.help_dlg_title)
                                 .setMessage(R.string.help_dlg_message)
                                 .setPositiveButton(android.R.string.ok, null)
                                 .create();
            ad.show();
            Linkify.addLinks((TextView) ad.findViewById(android.R.id.message),
                Linkify.ALL);
            return true;
        });

        Preference about = findPref("about");
        about.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), AboutActivity.class));
            return true;
        });

        autoStartFTPServer();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck();
        }
    }

    private void permissionCheck() {
        getPermissionUtil().init();
        getPermissionUtil().request();
    }

    private PermissionFragmentUtil getPermissionUtil() {
        if (null == permissionUtil)
            permissionUtil = new PermissionFragmentUtil(getActivity(), this);
        return permissionUtil;
    }

    private void autoStartFTPServer() {
        log("autoStartFTPServer");
        System.threadInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final ArrayList<String> deniedList = getPermissionUtil().getDeniedPermissionList();
            if (null != deniedList && deniedList.size() > 0) {
                log("you have denied permission(s),auto start stop ");
                return;
            }
        }
        if (FsService.isRunning()) {
            log("We are connecting to a new wifi network on a running server, ignore");
            return;
        }
        WifiManager wifiManager = (WifiManager)
                                      getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            log("Null wifi info received, bailing");
            return;
        }
        log("We are connected to " + wifiInfo.getSSID());
        if (FsSettings.isAutoConnectWifi(wifiInfo.getSSID())) {
            Intent start = new Intent(FsService.ACTION_START_FTPSERVER);
            start.setPackage(getActivity().getPackageName());
            getActivity().sendBroadcast(start);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String[] deniedStrings = permissionUtil.onRequestPermissionsResult(requestCode,
            permissions, grantResults);
        dealPermissionCheckResult(deniedStrings);
    }

    private void dealPermissionCheckResult(final String[] deniedStrings) {
        if (deniedStrings.length > 0) {
            final Context context = getActivity();
            StringBuilder builder = new StringBuilder();
            if (System.isZh(getActivity())) {
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
            showPermissionNoteDialog(msg);
        } else {
            log("all permission granted");
            autoStartFTPServer();
        }
    }

    private AlertDialog dialog;

    private void showPermissionNoteDialog(String msg) {
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new AlertDialog.Builder(getActivity())
                     .setTitle(R.string.permission_note)
                     .setMessage(msg)
                     .setCancelable(false)
                     .setPositiveButton(R.string.permission_bt_set, (dialog, i) -> {
                         dialog.dismiss();
                         appSetting();
                     })
                     .setNeutralButton(R.string.permission_bt_detail, (dialog, i) -> {
                         dialog.dismiss();
                         Intent intent = new Intent(getActivity(), PermissionActivity.class);
                         getActivity().startActivity(intent);
                     })
                     .setNegativeButton(R.string.permission_bt_exit, (dialog, i) -> {
                         dialog.dismiss();
                         MainActivity.appExit(getActivity());
                     })
                     .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void appSetting() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", getActivity().getPackageName(), null));
        if (System.isIntentAvailable(getActivity(), intent)) {
            startActivity(intent);
        } else {
            showMsg(getString(R.string.open_error_setting));
        }
    }

    private void showMsg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void createHandler() {
        mHandler = new Handler(getActivity().getMainLooper(), message -> {
            switch (message.what) {
                case MSG_FAIL_START:
                    showFailStartDialog();
                    break;
            }
            return false;
        });
    }

    private void showFailStartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.exit_title);
        builder.setMessage(R.string.dialog_fail_start_msg);
        builder.setPositiveButton(R.string.submit, null);
        builder.setNegativeButton(R.string.wifi_setting, (dialogInterface, i) -> {
            try {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.wifi_open_error, Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateRunningState();

        Cat.d("onResume: Register the preference change listner");
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);

        Cat.d("onResume: Registering the FTP server actions");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FsService.ACTION_STARTED);
        filter.addAction(FsService.ACTION_STOPPED);
        filter.addAction(FsService.ACTION_FAILEDTOSTART);
        getActivity().registerReceiver(mFsActionsReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        Cat.v("onPause: Unregistering the FTPServer actions");
        getActivity().unregisterReceiver(mFsActionsReceiver);

        Cat.d("onPause: Unregistering the preference change listner");
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        updateLoginInfo();
    }

    private void startServer() {
        Intent intent = new Intent(FsService.ACTION_START_FTPSERVER);
        intent.setPackage(getActivity().getPackageName());
        getActivity().sendBroadcast(intent);
    }

    private void stopServer() {
        Intent intent = new Intent(FsService.ACTION_STOP_FTPSERVER);
        intent.setPackage(getActivity().getPackageName());
        getActivity().sendBroadcast(intent);
    }

    private void updateLoginInfo() {

        String username = FsSettings.getUserName();
        String password = FsSettings.getPassWord();

        Cat.v("Updating login summary");
        PreferenceScreen loginPreference = findPref("login");
        loginPreference.setSummary(username + " : " + transformPassword(password));
        ((BaseAdapter) loginPreference.getRootAdapter()).notifyDataSetChanged();

        EditTextPreference usernamePref = findPref("username");
        usernamePref.setSummary(username);

        EditTextPreference passWordPref = findPref("password");
        passWordPref.setSummary(transformPassword(password));
    }

    private void updateRunningState() {
        Resources res = getResources();
        TwoStatePreference runningPref = findPref("running_switch");
        if (FsService.isRunning()) {
            runningPref.setChecked(true);
            // Fill in the FTP server address
            InetAddress address = FsService.getLocalInetAddress();
            if (address == null) {
                Cat.v("Unable to retrieve wifi ip address");
                runningPref.setSummary(R.string.running_summary_failed_to_get_ip_address);
                return;
            }
            String iptext = "ftp://" + address.getHostAddress() + ":"
                                + FsSettings.getPortNumber() + "/";
            String summary = res.getString(R.string.running_summary_started, iptext);
            runningPref.setSummary(summary);
            runningPref.setTitle(R.string.running_label);
        } else {
            runningPref.setChecked(false);
            runningPref.setSummary(R.string.running_summary_stopped);
            runningPref.setTitle(R.string.running_stop_label);
        }
    }

    /**
     * This receiver will check FTPServer.ACTION* messages and will update the button,
     * running_state, if the server is running and will also display at what url the
     * server is running.
     */
    private BroadcastReceiver mFsActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Cat.v("action received: " + intent.getAction());
            // remove all pending callbacks
            mHandler.removeCallbacksAndMessages(null);
            // action will be ACTION_STARTED or ACTION_STOPPED
            updateRunningState();
            // or it might be ACTION_FAILEDTOSTART
            final TwoStatePreference runningPref = findPref("running_switch");
            if (intent.getAction().equals(FsService.ACTION_FAILEDTOSTART)) {
                runningPref.setChecked(false);
                mHandler.sendEmptyMessage(MSG_FAIL_START);
                mHandler.postDelayed(
                    () -> runningPref.setSummary(R.string.running_summary_failed),
                    100);
                mHandler.postDelayed(
                    () -> runningPref.setSummary(R.string.running_summary_stopped),
                    5000);
            }
        }
    };

    static private String transformPassword(String password) {
        Context context = App.getAppContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();
        String showPasswordString = res.getString(R.string.show_password_default);
        boolean showPassword = showPasswordString.equals("true");
        showPassword = sp.getBoolean("show_password", showPassword);
        if (showPassword)
            return password;
        else {
            StringBuilder sb = new StringBuilder(password.length());
            for (int i = 0; i < password.length(); ++i)
                sb.append('*');
            return sb.toString();
        }
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    protected <T extends Preference> T findPref(CharSequence key) {
        return (T) findPreference(key);
    }

    private void uEvent(String id) {
        MobclickAgent.onEvent(getActivity(), id);
    }

    public void log(String msg) {
        Log.d(TAG, msg);
    }
}
