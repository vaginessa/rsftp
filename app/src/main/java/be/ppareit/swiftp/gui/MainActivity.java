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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import net.vrallev.android.cat.Cat;

import org.tuzhao.ftp.BuildConfig;
import org.tuzhao.ftp.R;
import org.tuzhao.ftp.activity.BaseActivity;
import org.tuzhao.ftp.util.WeakRunnable;

import be.ppareit.swiftp.App;
import be.ppareit.swiftp.FsService;
import be.ppareit.swiftp.FsSettings;

/**
 * This is the main activity for swiftp, it enables the user to start the server service
 * and allows the users to change the settings.
 */
public class MainActivity extends BaseActivity{

    final static int PERMISSIONS_REQUEST_CODE = 12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Cat.d("created");
        setTheme(FsSettings.getTheme());
        super.onCreate(savedInstanceState);

        if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }

        if (App.isFreeVersion() && App.isPaidVersionInstalled()) {
            Cat.d("Running demo while paid is installed");
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setTitle(R.string.demo_while_paid_dialog_title)
                    .setMessage(R.string.demo_while_paid_dialog_message)
                    .setPositiveButton(getText(android.R.string.ok),
                            (d, w) -> {
                                finish();
                            })
                    .create();
            ad.show();
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferenceFragment())
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            Cat.e("Unhandled request code");
            return;
        }
        Cat.d("permissions: " + permissions.toString());
        Cat.d("grantResults: " + grantResults.toString());
        if (grantResults.length > 0) {
            // Permissions not granted, close down
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Unable to proceed without the needed permissions, shuting down", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
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

            startActivity(email);

//            Toast.makeText(this, "Please use clear English!", Toast.LENGTH_LONG).show();
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
                             .setPositiveButton(R.string.submit, (dialogInterface, i) -> {
                                 getActivity().sendBroadcast(new Intent(FsService.ACTION_STOP_FTPSERVER));
                                 getActivity().finish();
                                 new Handler().postDelayed(new ExitRunnable(getActivity()), 1000);
                             })
                             .setNegativeButton(R.string.cancel, null)
                             .create();
        ad.show();
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
