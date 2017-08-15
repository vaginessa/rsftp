/*******************************************************************************
 * Copyright (c) 2013 Pieter Pareit.
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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import net.vrallev.android.cat.Cat;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.util.WeakRunnable;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import be.ppareit.swiftp.FsService;

/**
 * Simple widget for FTP Server.
 *
 * @author ppareit
 */
public class FsWidgetProvider extends AppWidgetProvider {

    private static final String TAG = FsWidgetProvider.class.getSimpleName();

    private final ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received broadcast: " + intent.getAction());
        // watch for the broadcasts by the ftp server and update the widget if needed
        final String action = intent.getAction();
        if (action.equals(FsService.ACTION_STARTED)
                || action.equals(FsService.ACTION_STOPPED)) {
            pool.execute(new UpdateWidgetRunnable(context));
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d(TAG, "updated called");
        pool.execute(new UpdateWidgetRunnable(context));
    }

    private static class UpdateWidgetRunnable extends WeakRunnable<Context> {

        UpdateWidgetRunnable(Context context) {
            super(context);
        }

        @Override
        public void weakRun(Context context) {
            Log.d(TAG, "UpdateService start command");
            // depending on whether or not the server is running, choose correct properties
            final String action;
            final int drawable;
            final String text;
            if (FsService.isRunning()) {
                action = FsService.ACTION_STOP_FTPSERVER;
                drawable = R.drawable.widget_on;
                // get ip address
                InetAddress address = FsService.getLocalInetAddress();
                if (address == null) {
                    Cat.w("Unable to retrieve the local ip address");
                    text = "ERROR";
                } else {
                    text = context.getString(R.string.running);
                }
            } else {
                action = FsService.ACTION_START_FTPSERVER;
                drawable = R.drawable.widget_off;
                text = context.getString(R.string.swiftp_name);
            }
            Intent startIntent = new Intent(action);
            startIntent.setPackage(context.getPackageName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                startIntent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            // setup the info on the widget
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
            views.setImageViewResource(R.id.widget_button, drawable);
            views.setTextViewText(R.id.widget_text, text);
            // new info is on widget, update it
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName widget = new ComponentName(context, FsWidgetProvider.class);
            manager.updateAppWidget(widget, views);
            // service has done it's work, android may kill it
        }
    }

}
