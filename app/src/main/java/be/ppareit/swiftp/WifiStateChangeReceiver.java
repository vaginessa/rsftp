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
        Cat.d("action: " + intent.getAction());
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info == null) {
            Cat.e("Null network info received, bailing");
            return;
        }
        if (info.isConnected()) {
            if (WifiStateUtil.isCanRunStart()) {
                WifiStateUtil.setCanRunStart(false);
                Cat.d("We are connecting to a wifi network");
                pool.execute(new StartServerRunnable(context));
            } else {
                Cat.w("Runnable START is running,ignore this request...");
            }
        } else {
            if (WifiStateUtil.isCanRunStop()) {
                WifiStateUtil.setCanRunStop(false);
                final PendingResult async = goAsync();
                final int time = FsSettings.getDisconnectWaitTime();
                Cat.d("We are disconnected from wifi network, wait time: " + time + " second(s)");
                pool.execute(new StopServerRunnable(context, async, time));
            } else {
                Cat.w("Runnable STOP is running,ignore this request...");
            }
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
                setCanRun();
                return;
            }
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (null == wifiManager) {
                Cat.e("WifiManager is null,what happened?");
                setCanRun();
                return;
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo == null) {
                Cat.e("Null wifi info received, bailing");
                setCanRun();
                return;
            }
            Cat.d("We are connected to " + wifiInfo.getSSID());
            if (FsSettings.isAutoConnectWifi(wifiInfo.getSSID())) {
                // sleep a short while so the network has time to truly connect
                Util.sleepIgnoreInterrupt(1000);
                Intent wifi = new Intent(FsService.ACTION_START_FTPSERVER);
                wifi.setPackage(context.getPackageName());
                context.sendBroadcast(wifi);
            }
            setCanRun();
        }

        private void setCanRun() {
            WifiStateUtil.setCanRunStart(true);
        }
    }

    private static class StopServerRunnable extends WeakRunnable<Context> {

        private final String TAG = "StopServerRunnable";

        private PendingResult async;
        private int time;

        StopServerRunnable(Context context, PendingResult async, int time) {
            super(context);
            this.async = async;
            this.time = time;
        }

        @Override
        public void weakRun(Context context) {
            System.threadInfo();
            if (!FsService.isRunning()) {
                finish();
                setCanRun();
                return;
            }

            if (this.time > 0) {
                Util.sleepIgnoreInterrupt(time * 1000);
            }
            if (!FsService.isRunning()) {
                finish();
                setCanRun();
                return;
            }

            ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
            if (null == conManager) {
                finish();
                setCanRun();
                return;
            }

            NetworkInfo netInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (null == netInfo) {
                finish();
                setCanRun();
                return;
            }

            if (netInfo.isConnectedOrConnecting()) {
                finish();
                setCanRun();
                return;
            }

            Cat.d("Wifi connection disconnected and no longer connecting, stopping the ftp server");
            Intent wifi = new Intent(FsService.ACTION_STOP_FTPSERVER);
            wifi.setPackage(context.getPackageName());
            context.sendBroadcast(wifi);
            finish();
            setCanRun();
        }

        private void finish() {
            try {
                if (null != async)
                    async.finish();
                this.async = null;
                Log.v(TAG, "goAsync finish");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setCanRun() {
            WifiStateUtil.setCanRunStop(true);
        }
    }

}
