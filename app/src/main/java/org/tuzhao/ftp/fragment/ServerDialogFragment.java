package org.tuzhao.ftp.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
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
import org.tuzhao.ftp.adapter.DownloadItemRecyclerAdapter;

import java.text.MessageFormat;

/**
 * author: tuzhao
 * 2017-09-18 22:50
 */
public abstract class ServerDialogFragment extends DialogFragment {

    static final String EXTRA_COUNT_TOTAL = "extra_total";

    private TextView mTitleTv;
    private TextView mTotalTv;
    private TextView mSuccessfulTv;
    private TextView mFailureTv;
    private TextView mNumTv;
    private ProgressBar mPb;
    private RecyclerView mRv;

    private int countTotal;

    private BroadcastReceiver receiver;
    private DownloadItemRecyclerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");
        Bundle bundle = getArguments();
        if (null != bundle) {
            setCountTotal(bundle.getInt(EXTRA_COUNT_TOTAL, 0));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setReceiver();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.server_dialog, null, false);

        mTitleTv = view.findViewById(R.id.server_dialog_title_tv);
        mTotalTv = view.findViewById(R.id.server_dialog_total_tv);
        mSuccessfulTv = view.findViewById(R.id.server_dialog_successful_tv);
        mFailureTv = view.findViewById(R.id.server_dialog_failure_tv);
        mPb = view.findViewById(R.id.server_dialog_pb);
        mNumTv = view.findViewById(R.id.server_dialog_progress_tv);
        mRv = view.findViewById(R.id.server_dialog_rv);
        dialog.setView(view);

        updateInterface(null, 0, 0, 0, 0);
        adapter = setAdapter();
        mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRv.setAdapter(adapter);

        return dialog;
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

    protected abstract DownloadItemRecyclerAdapter setAdapter();

    protected abstract String setDesTitle();

    protected abstract String setDesTotal();

    protected abstract String setDesSuccessful();

    protected abstract String setDesFailure();

    private String desTitle, desTotal, desSuccessful, desFailure;

    private void updateInterface(int complete, int failure, int progress) {
        if (null == desTitle)
            desTitle = setDesTitle();
        if (null == desTotal)
            desTotal = setDesTotal();
        if (null == desSuccessful)
            desSuccessful = setDesSuccessful();
        if (null == desFailure)
            desFailure = setDesFailure();
        if (null != mTitleTv)
            mTitleTv.setText(desTitle);
        if (null != mTotalTv)
            mTotalTv.setText(String.format(desTotal, String.valueOf(countTotal)));
        if (null != mSuccessfulTv)
            mSuccessfulTv.setText(String.format(desSuccessful, String.valueOf(complete)));
        if (null != mFailureTv)
            mFailureTv.setText(String.format(desFailure, String.valueOf(failure)));
        if (null != mPb)
            mPb.setProgress(progress);
        if (null != mNumTv)
            mNumTv.setText(MessageFormat.format("{0}%", String.valueOf(progress)));
    }

    private void setCountTotal(int count) {
        this.countTotal = count;
    }

    public int getCountTotal() {
        return this.countTotal;
    }

    protected abstract BroadcastReceiver setBroadcastReceiver();

    protected abstract IntentFilter setIntentFilter();

    private void setReceiver() {
        this.receiver = setBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, setIntentFilter());
    }

    public void updateInterface(String file, int status, int complete, int failure, int progress) {
        updateInterface(complete, failure, progress);
        if (null != adapter && null != file) {
            adapter.update(file, status);
            mRv.smoothScrollToPosition(adapter.getItemCount());
        }
    }

    public void log(String msg) {
        Log.d(this.getClass().getSimpleName(), msg);
    }

}
