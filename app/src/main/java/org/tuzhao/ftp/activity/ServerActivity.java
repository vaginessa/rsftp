package org.tuzhao.ftp.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.tuzhao.ftp.Fragment.ServerAddFragment;
import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerAddItem;
import org.tuzhao.ftp.service.ServerConnectService;

public class ServerActivity extends BaseActivity implements ServerAddFragment.OnCompleteListener {

    public static final String ACTION_SERVER_CONNECT = "action_server_connect";
    public static final String EXTRA_RESULT = "extra_result";

    private ServerConnectConnection connection;
    private ServerConnectService.ServerConnectBinder binder;

    private ServerAddFragment addFragment;
    private ServerBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.server_add) {
            if (null == addFragment) {
                addFragment = ServerAddFragment.newInstance();
                addFragment.setOnCompleteListener(this);
            }
            if (!addFragment.isShowing()) {
                addFragment.show(getFragmentManager(), "ServerAddFragment");
            }
        }
        return true;
    }

    @Override
    public void onComplete(ServerAddItem server) {
        if (null != binder) {
            showLoadingDialog();
            binder.connect(server);
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
        Intent intent = new Intent(ServerActivity.ACTION_SERVER_CONNECT);
        intent.putExtra(EXTRA_RESULT, result);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.sendBroadcast(intent);
    }

}
