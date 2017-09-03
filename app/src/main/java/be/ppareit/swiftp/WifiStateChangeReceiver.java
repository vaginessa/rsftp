/*
Copyright 2011-2015 Pieter Pareit

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package be.ppareit.swiftp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import net.vrallev.android.cat.Cat;

import org.tuzhao.ftp.util.System;
import org.tuzhao.ftp.util.WeakRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Detect if we get on a wifi network and possible start server.
 */
public class WifiStateChangeReceiver extends BroadcastReceiver {

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info == null) {
            Cat.e("Null network info received, bailing");
            return;
        }
        if (info.isConnected()) {
            Cat.d("We are connecting to a wifi network");
            pool.execute(new StartServerRunnable(context));
        } else {
            final PendingResult async = goAsync();
            Cat.d("We are disconnected from wifi network");
            pool.execute(new StopServerRunnable(context, async));
        }
    }

    private static class StartServerRunnable extends WeakRunnable<Context> {

        StartServerRunnable(Context context) {
            super(context);
        }

        @Override
        public void weakRun(Context context) {
            System.threadInfo();
            if (FsService.isRunning()) {
                Cat.v("We are connecting to a new wifi network on a running server, ignore");
                return;
            }
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo == null) {
                Cat.e("Null wifi info received, bailing");
                return;
            }
            Cat.d("We are connected to " + wifiInfo.getSSID());
            if (FsSettings.isAutoConnectWifi(wifiInfo.getSSID())){
                // sleep a short while so the network has time to truly connect
                Util.sleepIgnoreInterrupt(1000);
                Intent wifi = new Intent(FsService.ACTION_START_FTPSERVER);
                wifi.setPackage(context.getPackageName());
                context.sendBroadcast(wifi);
            }
        }
    }

    private static class StopServerRunnable extends WeakRunnable<Context> {

        private final String TAG = "StopServerRunnable";

        private final PendingResult async;

        StopServerRunnable(Context context, PendingResult async) {
            super(context);
            this.async = async;
        }

        @Override
        public void weakRun(Context context) {
            System.threadInfo();
            if (!FsService.isRunning()) {
                finish();
                return;
            }

            Util.sleepIgnoreInterrupt(15000);
            if (!FsService.isRunning()) {
                finish();
                return;
            }

            ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (netInfo.isConnectedOrConnecting()) {
                finish();
                return;
            }

            Cat.d("Wifi connection disconnected and no longer connecting, stopping the ftp server");
            Intent wifi = new Intent(FsService.ACTION_STOP_FTPSERVER);
            wifi.setPackage(context.getPackageName());
            context.sendBroadcast(wifi);
            finish();
        }

        private void finish() {
            try {
                if (null != async)
                    async.finish();
                Log.v(TAG, "goAsync finish");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
