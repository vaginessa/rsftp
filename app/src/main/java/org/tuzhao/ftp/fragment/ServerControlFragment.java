package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.util.ServerControlDialogAdapter;

/**
 * author: tuzhao
 * 2017-08-12 22:51
 */
public final class ServerControlFragment extends DialogFragment {

    private static final String FRAGMENT_TAG = "ServerControlFragment";
    private static final String TAG = "ServerControlFragment";
    private static final String EXTRA_DATA = "extra_data";

    private OnMenuClickListener listener;

    private ServerEntity entity;

    public static void show(Activity context, ServerEntity entity, OnMenuClickListener listener) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        ServerControlFragment fragment = newInstance();
        fragment.setOnMenuClickListener(listener);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_DATA, entity);
        fragment.setArguments(bundle);
        fragment.show(manager, FRAGMENT_TAG);
    }

    public ServerControlFragment() {
    }

    private static ServerControlFragment newInstance() {
        return new ServerControlFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        entity = bundle.getParcelable(EXTRA_DATA);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.setting);
        builder.setTitle(entity.getAddress());
        ServerControlDialogAdapter adapter = new ServerControlDialogAdapter(getActivity());
        builder.setAdapter(adapter, (dialogInterface, i) -> {
            log("position: " + i);
            if (null != listener)
                listener.onMenu(entity, i);
        });
        return builder.create();
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.listener = listener;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    public interface OnMenuClickListener {
        void onMenu(ServerEntity server, int postion);
    }


}
