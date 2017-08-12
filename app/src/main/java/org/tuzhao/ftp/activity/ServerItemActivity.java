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

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.service.ServerConnectService;

public class ServerItemActivity extends BaseActivity {

    public static final String ACTION_SERVER_CONNECT = "action_server_connect";
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
        startConnectService();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SERVER_CONNECT);
        receiver = new ServerBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void startConnectService() {
        connection = new ServerConnectConnection();
        Intent intent = new Intent(this, ServerConnectService.class);
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

    private class ServerConnectConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (ServerConnectService.ServerConnectBinder) iBinder;
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
            if (action.equals(ACTION_SERVER_CONNECT)) {
                dismissLoadingDialog();
                boolean result = intent.getBooleanExtra(EXTRA_RESULT, false);
                showMsg("server add result: " + result);
            }
        }
    }

    public static void sendConnectResult(Context context, boolean result) {
        Intent intent = new Intent(ServerItemActivity.ACTION_SERVER_CONNECT);
        intent.putExtra(EXTRA_RESULT, result);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.sendBroadcast(intent);
    }

}
