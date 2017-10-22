package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.tuzhao.ftp.R;

/**
 * author: tuzhao
 * 2017-10-22 21:28
 */
public class RenameDialogFragment extends BaseDialogFragment {

    private static final String FRAGMENT_TAG = "RenameDialogFragment";
    private static final String EXTRA_FILE_NAME = "extra_file_name";

    public static void show(Activity context, String fileName) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        RenameDialogFragment fragment = newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_FILE_NAME, fileName);
        fragment.setArguments(bundle);
        fragment.show(manager, FRAGMENT_TAG);
    }

    public static RenameDialogFragment newInstance() {
        return new RenameDialogFragment();
    }

    public RenameDialogFragment() {
    }

    private EditText mRenameEt;
    private String oldName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.modify_file_title);
        builder.setMessage(String.format(getString(R.string.modify_file_message), oldName));
        builder.setPositiveButton(R.string.submit, (dialog, which) -> {
            dialog.dismiss();
            if (null != mRenameEt) {
                String newName = mRenameEt.getText().toString().trim();
                final Activity activity = getActivity();
                if (null != activity && activity instanceof onEditResultListener) {
                    ((onEditResultListener) activity).onRename(oldName, newName);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_rename, null, false);
        mRenameEt = view.findViewById(R.id.server_dialog_rename);
        dialog.setView(view);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (null != arguments) {
            oldName = arguments.getString(EXTRA_FILE_NAME, "");
        } else {
            Log.w(FRAGMENT_TAG, "get file name error!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface onEditResultListener {
        void onRename(String oldName, String newName);
    }

    public void log(String msg) {
        Log.d(FRAGMENT_TAG, msg);
    }
}
