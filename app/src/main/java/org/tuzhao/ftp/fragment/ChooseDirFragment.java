package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.util.OnItemClickListener;
import org.tuzhao.ftp.util.ServerItemRecyclerAdapter;
import org.tuzhao.ftp.util.System;

import java.io.File;
import java.util.ArrayList;

/**
 * author: tuzhao
 * 2017-08-21 20:02
 */
public class ChooseDirFragment extends DialogFragment implements OnItemClickListener {

    private static final String FRAGMENT_TAG = "ChooseDirFragment";

    public static void show(Activity context) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        ChooseDirFragment fragment = newInstance();
        fragment.show(manager, FRAGMENT_TAG);
    }

    public ChooseDirFragment() {
    }

    public static ChooseDirFragment newInstance() {
        return new ChooseDirFragment();
    }

    private LinearLayout mStateLl;
    private TextView mCountTv;
    private ScrollView mSv;
    private TextView mPathTv;
    private TextView mSizeTv;
    private RecyclerView mRv;

    private ArrayList<RsFile> filesList;
    private ServerItemRecyclerAdapter adapter;

    private String mCurrentPath;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Dir");
        builder.setPositiveButton(R.string.submit, null);
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_server, null, false);
        mStateLl = view.findViewById(R.id.server_item_state_ll);
        mCountTv = view.findViewById(R.id.server_item_count_tv);
        mSv = view.findViewById(R.id.server_item_sv);
        mPathTv = view.findViewById(R.id.server_item_path_tv);
        mSizeTv = view.findViewById(R.id.server_item_size_tv);
        mRv = view.findViewById(R.id.server_item_rv);
        dialog.setView(view);
        return dialog;
    }

    private static void log(String msg) {
        Log.d(FRAGMENT_TAG, msg);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateAllToDefault();

        filesList = new ArrayList<>();
        adapter = new ServerItemRecyclerAdapter(getActivity(), filesList);
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.back, null);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log("click");
                if (null == mCurrentPath || TextUtils.isEmpty(mCurrentPath)) {
                    log("ignore...");
                } else if (mCurrentPath.endsWith("/")) {
                    log("root");
                } else {
                    int i = mCurrentPath.lastIndexOf("/");
                    if (i != 0) {
                        mCurrentPath = mCurrentPath.substring(0, i);
                    } else if (mCurrentPath.length() > 1) {
                        mCurrentPath = "/";
                    }
                    updateCurrentPath(mCurrentPath);
                    refresh();
                }
            }
        });
        adapter.setHeaderView(headerView);
        adapter.setOnItemClickListener(this);
        mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRv.setAdapter(adapter);

        File filesDir = getActivity().getFilesDir();
        mCurrentPath = filesDir.getAbsolutePath();
        refresh();
    }

    @Override
    public void onItemClick(View v, Object data, int position) {
        if (-1 != position) {
            RsFile ftpFile = filesList.get(position);
            if (ftpFile.isDir()) {
                String name = ftpFile.getName();
                if (mCurrentPath.equals("/")) {
                    mCurrentPath = mCurrentPath + name;
                } else {
                    mCurrentPath = mCurrentPath + "/" + name;
                }
                updateCurrentPath(mCurrentPath);
                refresh();
            }
        }
    }

    private void refresh() {
        File file = new File(mCurrentPath);
        ArrayList<RsFile> rsFiles = System.convertFileToRsFile(file.listFiles());
        filesList.clear();
        filesList.addAll(rsFiles);
        adapter.notifyDataSetChanged();
        updateFileCount(rsFiles.size());
        updateFolderSize(rsFiles);
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

}
