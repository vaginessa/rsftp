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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.apache.commons.net.ftp.FTPFile;
import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.service.ServerConnectService;
import org.tuzhao.ftp.util.FTPFileComparator;
import org.tuzhao.ftp.util.OnItemClickListener;
import org.tuzhao.ftp.util.OnItemLongClickListener;
import org.tuzhao.ftp.util.ServerItemRecyclerAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class ServerItemActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener {

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

    private RecyclerView mServerRv;
    private ArrayList<FTPFile> filesList;
    private ServerItemRecyclerAdapter adapter;

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

        filesList = new ArrayList<>();
        mServerRv = (RecyclerView) findViewById(R.id.server_item_rv);
        adapter = new ServerItemRecyclerAdapter(this, filesList);
        adapter.setOnItemLongClickListener(this);
        adapter.setOnItemClickListener(this);
        mServerRv.setLayoutManager(new LinearLayoutManager(this));
        mServerRv.setAdapter(adapter);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_server_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            if (null != binder) {
                binder.listFiles();
                showLoadingDialog();
            }
        }
        return true;
    }

    @Override
    public void onItemClick(View v, Object data, int position) {
        if (-1 != position) {
            FTPFile ftpFile = filesList.get(position);

        }
    }

    @Override
    public boolean onItemLongClick(View v, Object data, int position) {
        return false;
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

    private FTPFileComparator comparator;

    private class ServerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_SERVER_LIST_FILES)) {
                Serializable serializable = intent.getSerializableExtra(EXTRA_RESULT);
                if (null != serializable) {
                    ArrayList<FTPFile> list = (ArrayList<FTPFile>) serializable;
                    if (null == comparator) {
                        comparator = new FTPFileComparator();
                    }
                    Collections.sort(list, comparator);
                    log("array length: " + list.size());
                    filesList.clear();
                    filesList.addAll(list);
                    adapter.notifyDataSetChanged();
                }
            }
            dismissLoadingDialog();
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
