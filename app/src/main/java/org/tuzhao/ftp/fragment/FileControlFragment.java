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
import org.tuzhao.ftp.adapter.FileControlDialogAdapter;
import org.tuzhao.ftp.entity.RsMenu;

/**
 * author: tuzhao
 * 2017-09-12 21:30
 */
public final class FileControlFragment extends DialogFragment {

    private static final String FRAGMENT_TAG = "FileControlFragment";
    private static final String TAG = "FileControlFragment";
    private static final String EXTRA_DATA_POSITION = "extra_data_position";
    private static final String EXTRA_DATA_NAME = "extra_data_name";
    private static final String EXTRA_DATA_ICON = "extra_data_icon";

    private OnMenuClickListener listener;
    private String name;
    private int position;
    private int icon;

    public static void show(Activity context, String name, int icon, int position,
                            OnMenuClickListener listener) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        FileControlFragment fragment = newInstance();
        fragment.setOnMenuClickListener(listener);
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_DATA_POSITION, position);
        bundle.putInt(EXTRA_DATA_ICON, icon);
        bundle.putString(EXTRA_DATA_NAME, name);
        fragment.setArguments(bundle);
        fragment.show(manager, FRAGMENT_TAG);
    }

    public FileControlFragment() {
    }

    private static FileControlFragment newInstance() {
        return new FileControlFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        this.position = bundle.getInt(EXTRA_DATA_POSITION, -1);
        this.icon = bundle.getInt(EXTRA_DATA_ICON, R.drawable.file_unknown);
        this.name = bundle.getString(EXTRA_DATA_NAME, "");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        log("activity: " + getActivity().getClass().getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(this.icon);
        builder.setTitle(name);
        FileControlDialogAdapter adapter = new FileControlDialogAdapter(getActivity());
        builder.setAdapter(adapter, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            if (null != listener) {
                RsMenu item = RsMenu.Open;
                switch (i) {
                    case 0:
                        item = RsMenu.Details;
                        break;
                    case 1:
                        item = RsMenu.Rename;
                        break;
                    case 2:
                        item = RsMenu.Delete;
                        break;
                    case 3:
                        item = RsMenu.Open;
                        break;
                }
                listener.onMenu(item, this.position);
            }
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
        void onMenu(RsMenu menu, int position);
    }


}
