/*
Copyright 2011-2013 Pieter Pareit

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

package be.ppareit.swiftp.gui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import net.vrallev.android.cat.Cat;

import org.tuzhao.ftp.R;

import java.net.InetAddress;
import java.util.List;

import be.ppareit.swiftp.FsService;
import be.ppareit.swiftp.FsSettings;

public class FsNotification extends BroadcastReceiver {

    private static final String CHANNEL_ID = "ftp_channel_01";

    private final int NOTIFICATION_ID = 7890;

    @Override
    public void onReceive(Context context, Intent intent) {
        Cat.d("onReceive broadcast: " + intent.getAction());
        switch (intent.getAction()) {
            case FsService.ACTION_STARTED:
                setupNotification(context);
                break;
            case FsService.ACTION_STOPPED:
                clearNotification(context);
                break;
        }
    }

    @SuppressLint("NewApi")
    private void setupNotification(Context context) {
        Cat.d("Setting up the notification");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isChannelCreated(context)) {
                createChannel(context);
            }
        }
        // Get NotificationManager reference
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);

        // get ip address
        InetAddress address = FsService.getLocalInetAddress();
        if (address == null) {
            Cat.w("Unable to retrieve the local ip address");
            return;
        }
        String iptext = "ftp://" + address.getHostAddress() + ":"
                + FsSettings.getPortNumber() + "/";

        // Instantiate a Notification
        int icon = R.mipmap.notification;
        CharSequence tickerText = String.format(context.getString(R.string.notification_server_starting), iptext);
        long when = System.currentTimeMillis();

        // Define Notification's message and Intent
        CharSequence contentTitle = context.getString(R.string.notification_title);
        CharSequence contentText = String.format(context.getString(R.string.notification_text), iptext);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        int stopIcon = android.R.drawable.ic_menu_close_clear_cancel;
        CharSequence stopText = context.getString(R.string.notification_stop_text);
        Intent stopIntent = new Intent(FsService.ACTION_STOP_FTPSERVER);
        stopIntent.setPackage(context.getPackageName());
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0,
                stopIntent, PendingIntent.FLAG_ONE_SHOT);

        int preferenceIcon = android.R.drawable.ic_menu_preferences;
        CharSequence preferenceText = context.getString(R.string.notif_settings_text);
        Intent preferenceIntent = new Intent(context, MainActivity.class);
        preferenceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent preferencePendingIntent = PendingIntent.getActivity(context, 0, preferenceIntent, 0);

        Notification.Builder nb = new Notification.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setWhen(when)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nb.setChannelId(CHANNEL_ID);
        }

        Notification notification;

        // go from high to low android version adding extra options
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            nb.setVisibility(Notification.VISIBILITY_PUBLIC);
            nb.setCategory(Notification.CATEGORY_SERVICE);
            nb.setPriority(Notification.PRIORITY_MAX);
        }
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            nb.addAction(stopIcon, stopText, stopPendingIntent);
            nb.addAction(preferenceIcon, preferenceText, preferencePendingIntent);
            nb.setShowWhen(false);
            notification = nb.build();
        } else {
            notification = nb.getNotification();
        }

        // Pass Notification to NotificationManager
        nm.notify(NOTIFICATION_ID, notification);

        Cat.d("Notification setup done");
    }


    private void clearNotification(Context context) {
        Cat.d("Clearing the notifications");
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) context.getSystemService(ns);
        nm.cancelAll();
        Cat.d("Cleared notification");
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String name = "FTP";
            String description = "ftp open state channel";
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.GREEN);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            manager.createNotificationChannel(channel);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            Cat.d("create channel end");
        }
    }

    private boolean isChannelCreated(Context context) {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            List<NotificationChannel> channelList = manager.getNotificationChannels();
            Cat.d("channel list size: " + channelList.size());
            for (int i = 0; i < channelList.size(); i++) {
                NotificationChannel channel = channelList.get(i);
                String id = channel.getId();
                if (id.equals(CHANNEL_ID)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }
}
