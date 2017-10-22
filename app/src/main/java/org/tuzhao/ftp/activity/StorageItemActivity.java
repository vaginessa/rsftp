package org.tuzhao.ftp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.fragment.UploadDialogFragment;
import org.tuzhao.ftp.service.StorageUploadService;
import org.tuzhao.ftp.swipe.SwipeBackActivity;
import org.tuzhao.ftp.swipe.SwipeBackLayout;
import org.tuzhao.ftp.util.OnItemClickListener;
import org.tuzhao.ftp.util.RsFileComparator;
import org.tuzhao.ftp.adapter.ServerItemRecyclerAdapter;
import org.tuzhao.ftp.util.System;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class StorageItemActivity extends SwipeBackActivity implements OnItemClickListener {

    private static final String ACCESS_DIR_PATH = "access_dir_path";
    private static final String SERVER_DIR_PATH = "server_dir_path";
    private static final String SERVER_INFO = "server_info";

    public static void start(Activity context, ServerEntity entity, String dir, String serverPath) {
        Intent intent = new Intent(context, StorageItemActivity.class);
        intent.putExtra(ACCESS_DIR_PATH, dir);
        intent.putExtra(SERVER_DIR_PATH, serverPath);
        intent.putExtra(SERVER_INFO, (Parcelable) entity);
        context.startActivity(intent);
    }

    private LinearLayout mStateLl;
    private TextView mCountTv;
    private ScrollView mSv;
    private TextView mPathTv;
    private TextView mSizeTv;
    private TextView mNoteTv;
    private RecyclerView mRv;

    private ArrayList<RsFile> filesList;
    private RsFileComparator comparator;
    private ServerItemRecyclerAdapter adapter;

    private String mCurrentPath;
    private String serverPath;
    private StorageUploadService.StorageUploadBinder binder;
    private StorageUploadConnection connection;

    private ServerEntity server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_item);
        setDragEdge(SwipeBackLayout.DragEdge.LEFT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.storage_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mCurrentPath = getIntent().getStringExtra(ACCESS_DIR_PATH);
        serverPath = getIntent().getStringExtra(SERVER_DIR_PATH);
        server = getIntent().getParcelableExtra(SERVER_INFO);

        mStateLl = (LinearLayout) findViewById(R.id.server_item_state_ll);
        mCountTv = (TextView) findViewById(R.id.server_item_count_tv);
        mSv = (ScrollView) findViewById(R.id.server_item_sv);
        mPathTv = (TextView) findViewById(R.id.server_item_path_tv);
        mSizeTv = (TextView) findViewById(R.id.server_item_size_tv);
        mRv = (RecyclerView) findViewById(R.id.server_item_rv);
        mNoteTv = (TextView) findViewById(R.id.server_item_note_tv);

        startService();
        updateAllToDefault();
    }

    private void startService() {
        connection = new StorageUploadConnection();
        Intent intent = new Intent(this, StorageUploadService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != connection)
            unbindService(connection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_storage_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                break;
            case R.id.menu_upload:
                uploadSelectedFile();
                break;
            case R.id.menu_cloud:
                sendSwipeBackBroadcast(1.0f);
                finish();
                break;
            case R.id.menu_select_all:
                if (null != filesList && null != adapter) {
                    for (int i = 0; i < filesList.size(); i++) {
                        RsFile file = filesList.get(i);
                        file.setSelected(file.isFile());
                    }
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.menu_select_invert:
                if (null != filesList && null != adapter) {
                    for (int i = 0; i < filesList.size(); i++) {
                        RsFile file = filesList.get(i);
                        file.setSelected(file.isFile() && !file.getSelected());
                    }
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.menu_clear_select:
                if (null != filesList && null != adapter) {
                    for (int i = 0; i < filesList.size(); i++) {
                        RsFile file = filesList.get(i);
                        file.setSelected(false);
                    }
                    adapter.notifyDataSetChanged();
                }
                break;
        }
        return true;
    }

    private ArrayList<RsFile> selectedList;

    private void uploadSelectedFile() {
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
            String str1 = getString(R.string.upload_msg1);
            String str2 = getString(R.string.upload_msg2);
            String msg1 = String.format(str1, String.valueOf(count));
            String msg2 = String.format(str2, serverPath);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.note);
            builder.setMessage(msg1 + " " + msg2);
            builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {
                    UploadDialogFragment.show(getActivity(), count);
                    if (null != binder) {
                        binder.upload(server, selectedList, serverPath);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private class StorageUploadConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (StorageUploadService.StorageUploadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        filesList = new ArrayList<>();
        adapter = new ServerItemRecyclerAdapter(getActivity(), filesList);
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
        adapter.setOnItemClickListener(this);
        mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRv.setAdapter(adapter);

        refresh();
    }

    private void refresh() {
        updateCurrentPath(mCurrentPath);
        File file = new File(mCurrentPath);
        ArrayList<RsFile> rsFiles = System.convertFileToRsFile(file.listFiles());
        if (null == comparator) {
            comparator = new RsFileComparator();
        }
        Collections.sort(rsFiles, comparator);
        filesList.clear();
        filesList.addAll(rsFiles);
        adapter.notifyDataSetChanged();
        updateFileCount(rsFiles.size());
        updateFolderSize(rsFiles);
        updateNoteTv(file);
    }

    private void updateNoteTv(File file) {
        boolean ex = file.canExecute();
        boolean wr = file.canWrite();
        boolean re = file.canRead();
        StringBuilder builder = new StringBuilder();
        if (!ex) {
            builder.append(getString(R.string.dir_permission_x_failed));
            builder.append("\n");
        }
        if (!re) {
            builder.append(getString(R.string.dir_permission_r_failed));
            builder.append("\n");
        }
        if (!wr) {
            builder.append(getString(R.string.dir_permission_w_failed));
            builder.append("\n");
        }
        if (builder.toString().length() > 0) {
            if (null != mNoteTv) {
                mNoteTv.setText(builder.toString());
                mNoteTv.setVisibility(View.VISIBLE);
            }
        } else {
            if (null != mNoteTv) {
                mNoteTv.setText("");
                mNoteTv.setVisibility(View.INVISIBLE);
            }
        }
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

    @Override
    public void onItemClick(View v, Object data, int position) {
        if (-1 != position) {
            RsFile file = filesList.get(position);
            if (file.isDir()) {
                String name = file.getName();
                if (mCurrentPath.equals("/")) {
                    mCurrentPath = mCurrentPath + name;
                } else {
                    mCurrentPath = mCurrentPath + "/" + name;
                }
                refresh();
            } else if (file.isFile()) {
                file.setSelected(!file.getSelected());
                adapter.notifyDataSetChanged();
            }
        }
    }
}
