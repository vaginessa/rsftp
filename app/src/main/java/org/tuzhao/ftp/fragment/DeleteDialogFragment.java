package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.adapter.DeleteItemRecyclerAdapter;
import org.tuzhao.ftp.entity.Status;
import org.tuzhao.ftp.util.System;

import java.text.MessageFormat;

/**
 * author: tuzhao
 * 2017-08-21 20:02
 */
public class DeleteDialogFragment extends DialogFragment {

    private static final String FRAGMENT_TAG = "DeleteDialogFragment";

    private static final String EXTRA_COUNT_TOTAL = "extra_total";
    private static final String EXTRA_DELETE_STATUS = "extra_delete_status";
    private static final String EXTRA_DELETE_FILE = "extra_delete_file";

    private static final String ACTION_STATUS_DELETE = "delete_status_action";

    public static void show(Activity context, int count) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        DeleteDialogFragment fragment = newInstance();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_COUNT_TOTAL, count);
        fragment.setArguments(bundle);
        fragment.show(manager, FRAGMENT_TAG);
    }

    public static void sendStatusBroadCast(Context context, String fileName, int status) {
        Intent intent = new Intent(ACTION_STATUS_DELETE);
        intent.putExtra(EXTRA_DELETE_FILE, fileName);
        intent.putExtra(EXTRA_DELETE_STATUS, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class DeleteBroadcastReceiver extends BroadcastReceiver {

        private int statusSuccessful;
        private int statusFailure;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_STATUS_DELETE)) {
                String file = intent.getStringExtra(EXTRA_DELETE_FILE);
                int status = intent.getIntExtra(EXTRA_DELETE_STATUS, 0);
                if (status == Status.SUCC) {
                    statusSuccessful++;
                    updateServer(context);
                }
                if (status == Status.FAIL) {
                    statusFailure++;
                }
                int progress = (int) (((float) (statusSuccessful + statusFailure)) / countTotal * 100f);
                log("progress: " + progress);
                updateInterface(statusSuccessful, statusFailure, progress);
                if (null != adapter) {
                    adapter.update(file, status);
                    mRv.smoothScrollToPosition(adapter.getItemCount());
                }
            }
        }

        private void updateServer(Context context) {
            Intent intent = new Intent(System.ACTION_SERVER_CURRENT_UPDATE);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

    }

    public DeleteDialogFragment() {
    }

    public static DeleteDialogFragment newInstance() {
        return new DeleteDialogFragment();
    }

    private TextView mTotalTv;
    private TextView mSuccessfulTv;
    private TextView mFailureTv;
    private TextView mNumTv;
    private ProgressBar mPb;
    private RecyclerView mRv;

    private int countTotal;

    private DeleteBroadcastReceiver receiver;
    private DeleteItemRecyclerAdapter adapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log("onCreateDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.delete, null, false);

        mTotalTv = view.findViewById(R.id.delete_total_tv);
        mSuccessfulTv = view.findViewById(R.id.delete_successful_tv);
        mFailureTv = view.findViewById(R.id.delete_failure_tv);
        mPb = view.findViewById(R.id.delete_pb);
        mNumTv = view.findViewById(R.id.delete_num_tv);
        mRv = view.findViewById(R.id.delete_rv);
        dialog.setView(view);

        updateInterface(0, 0, 0);
        adapter = new DeleteItemRecyclerAdapter(getActivity());
        mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRv.setAdapter(adapter);

        return dialog;
    }

    private String desTotal, desSuccessful, desFailure;

    private void updateInterface(int complete, int failure, int progress) {
        if (null == desTotal) desTotal = getString(R.string.delete_total);
        if (null == desSuccessful) desSuccessful = getString(R.string.delete_successful);
        if (null == desFailure) desFailure = getString(R.string.delete_failure);
        if (null != mTotalTv) {
            mTotalTv.setText(String.format(desTotal, String.valueOf(countTotal)));
        }
        if (null != mSuccessfulTv) {
            mSuccessfulTv.setText(String.format(desSuccessful, String.valueOf(complete)));
        }
        if (null != mFailureTv) {
            mFailureTv.setText(String.format(desFailure, String.valueOf(failure)));
        }
        if (null != mPb)
            mPb.setProgress(progress);
        if (null != mNumTv)
            mNumTv.setText(MessageFormat.format("{0}%", String.valueOf(progress)));
    }

    private static void log(String msg) {
        Log.d(FRAGMENT_TAG, msg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");
        Bundle bundle = getArguments();
        if (null != bundle) {
            countTotal = bundle.getInt(EXTRA_COUNT_TOTAL, 0);
        }
        receiver = new DeleteBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STATUS_DELETE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
        log("onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
        if (null != receiver) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
    }

}
