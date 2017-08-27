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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.tuzhao.ftp.R;

import java.text.MessageFormat;

/**
 * author: tuzhao
 * 2017-08-21 20:02
 */
public class DownloadDialogFragment extends DialogFragment {

    private static final String FRAGMENT_TAG = "DownloadDialogFragment";
    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_COUNT_TOTAL = "extra_total";
    private static final String EXTRA_COUNT_COMPLETE = "extra_complete";
    private static final String EXTRA_COUNT_FAILURE = "extra_failure";
    private static final String EXTRA_COUNT_NAME = "extra_name";

    private static final String ACTION_PROCESS_DOWNLOAD = "download_process_action";

    public static void show(Activity context, int count, String firstFileName) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        DownloadDialogFragment fragment = newInstance();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_COUNT_TOTAL, count);
        bundle.putString(EXTRA_NAME, firstFileName);
        fragment.setArguments(bundle);
        fragment.show(manager, FRAGMENT_TAG);
    }

    public static void sendBroadCast(Context context, int total, int complete, int failure, String name) {
        Intent intent = new Intent(ACTION_PROCESS_DOWNLOAD);
        intent.putExtra(EXTRA_COUNT_TOTAL, total);
        intent.putExtra(EXTRA_COUNT_COMPLETE, complete);
        intent.putExtra(EXTRA_COUNT_FAILURE, failure);
        intent.putExtra(EXTRA_COUNT_NAME, name);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_PROCESS_DOWNLOAD)) {
                countTotal = intent.getIntExtra(EXTRA_COUNT_TOTAL, 0);
                int countSuccessful = intent.getIntExtra(EXTRA_COUNT_COMPLETE, 0);
                int countFailure = intent.getIntExtra(EXTRA_COUNT_FAILURE, 0);
                currentFile = intent.getStringExtra(EXTRA_COUNT_NAME);
                int progress = (int) (((float) (countFailure + countSuccessful)) / countTotal * 100f);
                log("progress: " + progress);
                updateInterface(countTotal, countSuccessful, countFailure, progress, currentFile);
            }
        }
    }

    public DownloadDialogFragment() {
    }

    public static DownloadDialogFragment newInstance() {
        return new DownloadDialogFragment();
    }

    private TextView mTotalTv;
    private TextView mSuccessfulTv;
    private TextView mFailureTv;
    private TextView mProcessTv;
    private TextView mNumTv;
    private ProgressBar mPb;

    private int countTotal;
    private String currentFile;

    private DownloadBroadcastReceiver receiver;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log("onCreateDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.download, null, false);

        mTotalTv = view.findViewById(R.id.download_total_tv);
        mSuccessfulTv = view.findViewById(R.id.download_successful_tv);
        mFailureTv = view.findViewById(R.id.download_failure_tv);
        mProcessTv = view.findViewById(R.id.download_process_tv);
        mPb = view.findViewById(R.id.download_pb);
        mNumTv = view.findViewById(R.id.download_num_tv);
        dialog.setView(view);

        updateInterface(countTotal, 0, 0, 0, currentFile);
        return dialog;
    }

    private String desTotal, desSuccessful, desFailure, desProcess;

    private void updateInterface(int total, int complete, int failure, int progress, String name) {
        if (null == desTotal) desTotal = getString(R.string.download_total);
        if (null == desSuccessful) desSuccessful = getString(R.string.download_successful);
        if (null == desFailure) desFailure = getString(R.string.download_failure);
        if (null == desProcess) desProcess = getString(R.string.download_in);
        if (null != mTotalTv) {
            mTotalTv.setText(String.format(desTotal, String.valueOf(total)));
        }
        if (null != mSuccessfulTv) {
            mSuccessfulTv.setText(String.format(desSuccessful, String.valueOf(complete)));
        }
        if (null != mFailureTv) {
            mFailureTv.setText(String.format(desFailure, String.valueOf(failure)));
        }
        if (null != mProcessTv) {
            mProcessTv.setText(String.format(desProcess, name));
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
            currentFile = bundle.getString(EXTRA_NAME, "");
        }
        receiver = new DownloadBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PROCESS_DOWNLOAD);
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
