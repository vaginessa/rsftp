package org.tuzhao.ftp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;
import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.RsFTPFile;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.fragment.DownloadDialogFragment;
import org.tuzhao.ftp.service.ServerConnectService;
import org.tuzhao.ftp.service.StorageUploadService;
import org.tuzhao.ftp.util.FTPFileComparator;
import org.tuzhao.ftp.util.OnItemClickListener;
import org.tuzhao.ftp.util.OnItemLongClickListener;
import org.tuzhao.ftp.util.ServerItemRecyclerAdapter;
import org.tuzhao.ftp.util.System;
import org.tuzhao.ftp.util.WeakRunnable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public final class ServerItemActivity extends BaseActivity implements OnItemClickListener,
                                                                          OnItemLongClickListener {

    private static final String ACTION_SERVER_LIST_FILES = "action_server_list_files";

    private static final String EXTRA_RESULT = "extra_result";
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

    private String mCurrentPath;
    private ArrayList<RsFile> filesList;
    private ServerItemRecyclerAdapter adapter;

    private TextView mCountTv;
    private TextView mPathTv;
    private TextView mSizeTv;
    private ScrollView mServerSv;
    private RecyclerView mServerRv;

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
        filter.addAction(System.ACTION_SERVER_CURRENT_PATH);
        filter.addAction(System.ACTION_SERVER_EXCEPTION_CONNECT);
        filter.addAction(System.ACTION_SERVER_EXCEPTION_LOGIN);
        filter.addAction(System.ACTION_SERVER_FAILED_LOGIN);
        receiver = new ServerBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        mServerSv = (ScrollView) findViewById(R.id.server_item_sv);
        mServerRv = (RecyclerView) findViewById(R.id.server_item_rv);
        mCountTv = (TextView) findViewById(R.id.server_item_count_tv);
        mPathTv = (TextView) findViewById(R.id.server_item_path_tv);
        mSizeTv = (TextView) findViewById(R.id.server_item_size_tv);

        updateAllToDefault();

        filesList = new ArrayList<>();
        adapter = new ServerItemRecyclerAdapter(this, filesList);
        setHeaderView();
        adapter.setOnItemLongClickListener(this);
        adapter.setOnItemClickListener(this);
        mServerRv.setLayoutManager(new LinearLayoutManager(this));
        mServerRv.setAdapter(adapter);

        startConnectService();
    }

    private void setHeaderView() {
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.back, null);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log("click");
                if (null == mCurrentPath || TextUtils.isEmpty(mCurrentPath)) {
                    showMsg(getString(R.string.path_empty));
                } else if (mCurrentPath.endsWith("/")) {
                    showMsg(getString(R.string.dir_root_note));
                } else {
                    int i = mCurrentPath.lastIndexOf("/");
                    if (i != 0) {
                        mCurrentPath = mCurrentPath.substring(0, i);
                    } else if (mCurrentPath.length() > 1) {
                        mCurrentPath = "/";
                    }
                    refresh();
                }
            }
        });
        adapter.setHeaderView(headerView);
    }

    private void startConnectService() {
        connection = new ServerConnectConnection();
        Intent intent = new Intent(this, ServerConnectService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != binder)
            binder.clear();
        if (null != connection)
            unbindService(connection);
        if (null != receiver) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
        if (null != scrollRunnable) {
            scrollRunnable.destroy();
        }
        stopService(new Intent(this, ServerConnectService.class));
        stopService(new Intent(this, StorageUploadService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_server_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_download:
                downloadSelectedFile();
                break;
            case R.id.menu_refresh:
                if (null != binder) {
                    refresh();
                }
                break;
            case R.id.menu_mobile:
                StorageItemActivity.start(this, server, server.getSavePath(), mCurrentPath);
                break;
        }
        return true;
    }

    private ArrayList<RsFile> selectedList;

    private void downloadSelectedFile() {
        if (null == selectedList) {
            selectedList = new ArrayList<>();
        }
        selectedList.clear();
        for (int i = 0; i < filesList.size(); i++) {
            RsFile file = filesList.get(i);
            if (file.getSelected()) {
                selectedList.add(file);
            }
        }
        if (selectedList.size() == 0) {
            showMsg(getString(R.string.selected_note));
        } else {
            final int count = selectedList.size();
            String str1 = getString(R.string.download_msg1);
            String str2 = getString(R.string.download_msg2);
            String msg1 = String.format(str1, String.valueOf(count));
            String msg2 = String.format(str2, server.getSavePath());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.note);
            builder.setMessage(msg1 + " " + msg2);
            builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {
                    if (null != binder) {
                        DownloadDialogFragment.show(getActivity(), count);
                        binder.download(selectedList, mCurrentPath);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private void showProgressDialog() {
    }

    @Override
    public void onItemClick(View v, Object data, int position) {
        if (-1 != position) {
            RsFile ftpFile = filesList.get(position);
            boolean w = ftpFile.canWrite();
            boolean r = ftpFile.canRead();
            boolean e = ftpFile.canExecute();
            log("permission: w=" + w + " r=" + r + " e=" + e);
            if (ftpFile.isDir()) {
                String name = ftpFile.getName();
                if (mCurrentPath.equals("/")) {
                    mCurrentPath = mCurrentPath + name;
                } else {
                    mCurrentPath = mCurrentPath + "/" + name;
                }
                updateCurrentPath(mCurrentPath);
                refresh();
            } else if (ftpFile.isFile()) {
                if (ftpFile instanceof RsFTPFile) {
                    RsFTPFile rsFTPFile = (RsFTPFile) ftpFile;
                    rsFTPFile.setSelected(!rsFTPFile.getSelected());
                    adapter.notifyDataSetChanged();
                }
            }
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
            binder.setServer(server);
            binder.listFiles(null);
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
    private ScrollRunnable scrollRunnable;

    private class ServerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_SERVER_LIST_FILES)) {
                Serializable serializable = intent.getSerializableExtra(EXTRA_RESULT);
                ArrayList<RsFile> list = null;
                if (null != serializable) {
                    ArrayList<FTPFile> ftpList = (ArrayList<FTPFile>) serializable;
                    if (null == comparator) {
                        comparator = new FTPFileComparator();
                    }
                    Collections.sort(ftpList, comparator);
                    log("array length: " + ftpList.size());
                    list = System.convertFTPFileToRsFile(ftpList);
                    filesList.clear();
                    filesList.addAll(list);
                    adapter.notifyDataSetChanged();
                }
                updateFileCount(list == null ? 0 : list.size());
                updateFolderSize(list);
                dismissLoadingDialog();
            } else if (action.equals(System.ACTION_SERVER_CURRENT_PATH)) {
                String path = System.getServerCurrentPath(intent);
                mCurrentPath = path;
                updateCurrentPath(path);
                if (null == scrollRunnable) {
                    scrollRunnable = new ScrollRunnable(getActivity(), mServerSv);
                }
                getDefaultHandler().post(scrollRunnable);
            } else if (action.equals(System.ACTION_SERVER_EXCEPTION_CONNECT)) {
                String msg = System.getServerErrorConnectMsg(intent);
                showNoteDialog(msg);
                dismissLoadingDialog();
            } else if (action.equals(System.ACTION_SERVER_EXCEPTION_LOGIN)) {
                String msg = System.getServerErrorLoginMsg(intent);
                showNoteDialog(msg);
                dismissLoadingDialog();
            } else if (action.equals(System.ACTION_SERVER_FAILED_LOGIN)) {
                String msg = getString(R.string.failed_login);
                showNoteDialog(msg);
                dismissLoadingDialog();
            }
        }
    }

    private static class ScrollRunnable extends WeakRunnable<Context> {

        private ScrollView view;

        ScrollRunnable(Context context, ScrollView view) {
            super(context);
            this.view = view;
        }

        @Override
        public void weakRun(Context context) {
            if (null != view)
                view.scrollTo(0, view.getMeasuredHeight());
        }

        @Override
        public void destroy() {
            view = null;
        }
    }

    private void refresh() {
        showLoadingDialog();
        updateAllToDefault();
        filesList.clear();
        adapter.notifyDataSetChanged();
        binder.listFiles(mCurrentPath);
    }

    private void updateAllToDefault() {
        updateCurrentPath("");
        updateFileCount(0);
        updateFolderSize(null);
    }

    private void updateCurrentPath(String path) {
        String des = getString(R.string.server_item_path);
        String real = (path == null ? this.getString(R.string.error) : path);
        if (null != mPathTv) {
            mPathTv.setText(String.format(des, real));
        }
    }

    private void updateFileCount(int count) {
        if (null != mCountTv)
            mCountTv.setText(String.format(getString(R.string.server_item_count), String.valueOf(count)));
    }

    @Override
    public void onBackPressed() {
        if (null == mCurrentPath || TextUtils.isEmpty(mCurrentPath)) {
            super.onBackPressed();
        } else if (mCurrentPath.endsWith("/")) {
            showLeaveDialog();
        } else {
            int i = mCurrentPath.lastIndexOf("/");
            if (i != 0) {
                mCurrentPath = mCurrentPath.substring(0, i);
            } else if (mCurrentPath.length() > 1) {
                mCurrentPath = "/";
            }
            refresh();
        }
    }

    private void showLeaveDialog() {
        AlertDialog ad = new AlertDialog.Builder(getActivity())
                             .setTitle(R.string.leave_title)
                             .setMessage(R.string.leave_msg)
                             .setPositiveButton(R.string.submit, (dialog, i) -> {
                                 dialog.dismiss();
                                 getActivity().finish();
                             })
                             .setNegativeButton(R.string.cancel, null)
                             .create();
        ad.show();
    }

    private void updateFolderSize(ArrayList<RsFile> list) {
        long size = 0;
        if (null != list) {
            for (int i = 0; i < list.size(); i++) {
                RsFile file = list.get(i);
                if (file.isFile())
                    size += file.getSize();
            }
        }
        String s = ServerItemRecyclerAdapter.getSize(size);
        if (null != mSizeTv)
            mSizeTv.setText(String.format(getString(R.string.server_item_size), s));
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
