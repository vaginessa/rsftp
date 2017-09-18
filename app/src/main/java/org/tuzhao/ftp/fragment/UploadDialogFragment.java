package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.adapter.DownloadItemRecyclerAdapter;
import org.tuzhao.ftp.adapter.UploadItemRecyclerAdapter;
import org.tuzhao.ftp.entity.Status;

/**
 * author: tuzhao
 * 2017-09-10 21:50
 */
public class UploadDialogFragment extends ServerDialogFragment {

    private static final String FRAGMENT_TAG = "UploadDialogFragment";

    private static final String EXTRA_UPLOAD_STATUS = "upload_status";
    private static final String EXTRA_UPLOAD_FILE = "upload_file";

    private static final String ACTION_STATUS_UPLOAD = "upload_status_action";

    public static void show(Activity context, int totalCount) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        UploadDialogFragment fragment = newInstance();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_COUNT_TOTAL, totalCount);
        fragment.setArguments(bundle);
        fragment.show(manager, FRAGMENT_TAG);
    }

    public static void sendStatusBroadCast(Context context, String name, int status) {
        Intent intent = new Intent(ACTION_STATUS_UPLOAD);
        intent.putExtra(EXTRA_UPLOAD_STATUS, status);
        intent.putExtra(EXTRA_UPLOAD_FILE, name);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class UploadBroadcastReceiver extends BroadcastReceiver {

        private int statusSuccessful;
        private int statusFailure;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_STATUS_UPLOAD)) {
                String file = intent.getStringExtra(EXTRA_UPLOAD_FILE);
                int status = intent.getIntExtra(EXTRA_UPLOAD_STATUS, 0);
                if (status == Status.SUCC) {
                    statusSuccessful++;
                }
                if (status == Status.FAIL) {
                    statusFailure++;
                }
                int progress = (int) (((float) (statusSuccessful + statusFailure)) / getCountTotal() * 100f);
                log("progress: " + progress);
                updateInterface(file, status, statusSuccessful, statusFailure, progress);
            }
        }

    }

    public UploadDialogFragment() {
    }

    public static UploadDialogFragment newInstance() {
        return new UploadDialogFragment();
    }

    @Override
    protected DownloadItemRecyclerAdapter setAdapter() {
        return new UploadItemRecyclerAdapter(getActivity());
    }

    @Override
    protected String setDesTitle() {
        return getString(R.string.upload_title);
    }

    @Override
    protected String setDesTotal() {
        return getString(R.string.upload_total);
    }

    @Override
    protected String setDesSuccessful() {
        return getString(R.string.upload_successful);
    }

    @Override
    protected String setDesFailure() {
        return getString(R.string.upload_failure);
    }

    @Override
    protected BroadcastReceiver setBroadcastReceiver() {
        return new UploadBroadcastReceiver();
    }

    @Override
    protected IntentFilter setIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STATUS_UPLOAD);
        return filter;
    }

}
