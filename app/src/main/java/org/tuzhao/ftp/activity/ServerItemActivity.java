package org.tuzhao.ftp.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import org.apache.commons.net.ftp.FTPFile;
import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.service.ServerConnectService;

import java.io.Serializable;
import java.util.ArrayList;

public class ServerItemActivity extends BaseActivity {

    public static final String ACTION_SERVER_LIST_FILES = "action_server_list_files";
    public static final String EXTRA_RESULT = "extra_result";
    private static final String EXTRA_START = "extra_start";


    private ServerConnectConnection connection;
    private ServerConnectService.ServerConnectBinder binder;

    private ServerBroadcastReceiver receiver;
    private ServerEntity server;

    /**
     * who want start ServerItemActivity must be call this method
     * @param context    Activity
     * @param parcelable ServerEntity
     */
    public static void start(Activity context, Parcelable parcelable) {
        if (!(parcelable instanceof ServerEntity)) {
            throw new RuntimeException("must give me a ServerEntity Instance!");
        }
        Intent intent = new Intent(context, ServerItemActivity.class);
        intent.putExtra(EXTRA_START, parcelable);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        Parcelable parcelable = getIntent().getParcelableExtra(EXTRA_START);
        if (null != parcelable && !(parcelable instanceof ServerEntity)) {
            showMsg("receive server info error");
            finish();
            return;
        } else {
            server = (ServerEntity) parcelable;
            log("start server info: " + server);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SERVER_LIST_FILES);
        receiver = new ServerBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        startConnectService();
    }

    private void startConnectService() {
        connection = new ServerConnectConnection();
        Intent intent = new Intent(this, ServerConnectService.class);
        intent.putExtra(ServerConnectService.EXTRA_START, (Parcelable) server);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != connection)
            unbindService(connection);
        if (null != receiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
    }

    public void test(View view) {
        if (null != binder)
            binder.listFiles();
    }

    private class ServerConnectConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (ServerConnectService.ServerConnectBinder) iBinder;
            binder.listFiles();
            showLoadingDialog();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {

        }
    }

    private class ServerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_SERVER_LIST_FILES)) {
                dismissLoadingDialog();
                Serializable serializable = intent.getSerializableExtra(EXTRA_RESULT);
                if (null != serializable) {
                    ArrayList<FTPFile> list = (ArrayList<FTPFile>) serializable;
                    log("array length: " + list.size());
                    for (int i = 0; i < list.size(); i++) {
                        FTPFile file = list.get(i);
                        String name = file.getName();
                        long size = file.getSize();
                        long timeInMillis = file.getTimestamp().getTimeInMillis();
                        log("name: " + name + " size: " + size + " time:" + timeInMillis);
                    }
                }
            }
        }
    }

    public static void sendListFilesResult(Context context, ArrayList<FTPFile> list) {
        Intent intent = new Intent(ServerItemActivity.ACTION_SERVER_LIST_FILES);
        if (list != null) {
            intent.putExtra(EXTRA_RESULT, list);
        }
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.sendBroadcast(intent);
    }

}
