package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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
import android.widget.Toast;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.util.RsFileComparator;
import org.tuzhao.ftp.util.OnItemClickListener;
import org.tuzhao.ftp.util.ServerItemRecyclerAdapter;
import org.tuzhao.ftp.util.System;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * author: tuzhao
 * 2017-08-21 20:02
 */
public class ChooseDirFragment extends DialogFragment implements OnItemClickListener {

    private static final String FRAGMENT_TAG = "ChooseDirFragment";
    private static final String EXTRA_PATH = "extra_path";

    public static void show(Activity context, String openPath, OnSelectListener listener) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        ChooseDirFragment fragment = newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PATH, openPath);
        fragment.setArguments(bundle);
        fragment.setOnSelectListener(listener);
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
    private TextView mNoteTv;
    private RecyclerView mRv;

    private ArrayList<RsFile> filesList;
    private RsFileComparator comparator;
    private ServerItemRecyclerAdapter adapter;

    private String mCurrentPath;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_dir);
        builder.setPositiveButton(R.string.submit, new SubmitClickListener());
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_server, null, false);
        mStateLl = view.findViewById(R.id.server_item_state_ll);
        mCountTv = view.findViewById(R.id.server_item_count_tv);
        mSv = view.findViewById(R.id.server_item_sv);
        mPathTv = view.findViewById(R.id.server_item_path_tv);
        mSizeTv = view.findViewById(R.id.server_item_size_tv);
        mRv = view.findViewById(R.id.server_item_rv);
        mNoteTv = view.findViewById(R.id.server_item_note_tv);
        dialog.setView(view);
        return dialog;
    }

    private class SubmitClickListener implements DialogInterface.OnClickListener {

        SubmitClickListener() {
        }

        @Override
        public void onClick(DialogInterface dialog, int position) {
            File file = new File(mCurrentPath);
            boolean ex = file.canExecute();
            boolean wr = file.canWrite();
            if (!ex) {
                showMsg(getString(R.string.dir_permission_x_failed));
            } else if (!wr) {
                showMsg(getString(R.string.dir_permission_w_failed));
            } else {
                log("select dir: " + mCurrentPath);
                getDialog().dismiss();
                if (null != listener) {
                    listener.onSelect(mCurrentPath);
                }
            }
        }
    }

    private static void log(String msg) {
        Log.d(FRAGMENT_TAG, msg);
    }

    private void showMsg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
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

        Bundle bundle = getArguments();
        if (null != bundle) {
            mCurrentPath = bundle.getString(EXTRA_PATH, "");
            log("external storage path: " + mCurrentPath);
        }
        if (null == mCurrentPath || mCurrentPath.equals("")) {
            File filesDir = getActivity().getFilesDir();
            mCurrentPath = filesDir.getAbsolutePath();
        }
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
                refresh();
            }
        }
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

    private OnSelectListener listener;

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    public interface OnSelectListener {
        void onSelect(String path);
    }

}
