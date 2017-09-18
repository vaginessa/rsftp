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
import org.tuzhao.ftp.entity.Status;

/**
 * author: tuzhao
 * 2017-08-21 20:02
 */
public class DownloadDialogFragment extends ServerDialogFragment {

    private static final String FRAGMENT_TAG = "DownloadDialogFragment";

    private static final String EXTRA_DOWNLOAD_STATUS = "extra_download_status";
    private static final String EXTRA_DOWNLOAD_FILE = "extra_download_file";

    private static final String ACTION_STATUS_DOWNLOAD = "download_status_action";

    public static void show(Activity context, int count) {
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
        fragment.setArguments(bundle);
        fragment.show(manager, FRAGMENT_TAG);
    }

    public static void sendStatusBroadCast(Context context, String fileName, int status) {
        Intent intent = new Intent(ACTION_STATUS_DOWNLOAD);
        intent.putExtra(EXTRA_DOWNLOAD_FILE, fileName);
        intent.putExtra(EXTRA_DOWNLOAD_STATUS, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        private int statusSuccessful;
        private int statusFailure;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_STATUS_DOWNLOAD)) {
                String file = intent.getStringExtra(EXTRA_DOWNLOAD_FILE);
                int status = intent.getIntExtra(EXTRA_DOWNLOAD_STATUS, 0);
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

    public DownloadDialogFragment() {
    }

    public static DownloadDialogFragment newInstance() {
        return new DownloadDialogFragment();
    }

    @Override
    protected DownloadItemRecyclerAdapter setAdapter() {
        return new DownloadItemRecyclerAdapter(getActivity());
    }

    @Override
    protected String setDesTitle() {
        return getString(R.string.download_title);
    }

    @Override
    protected String setDesTotal() {
        return getString(R.string.download_total);
    }

    @Override
    protected String setDesSuccessful() {
        return getString(R.string.download_successful);
    }

    @Override
    protected String setDesFailure() {
        return getString(R.string.download_failure);
    }

    @Override
    protected BroadcastReceiver setBroadcastReceiver() {
        return new DownloadBroadcastReceiver();
    }

    @Override
    protected IntentFilter setIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STATUS_DOWNLOAD);
        return filter;
    }

}
