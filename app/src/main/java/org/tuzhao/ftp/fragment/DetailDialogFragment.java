package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.FileInfo;

/**
 * author: tuzhao
 * 2017-11-05 21:30
 */
public class DetailDialogFragment extends BaseDialogFragment {

    private static final String FRAGMENT_TAG = "DetailDialogFragment";

    private static final String EXTRA_DATA = "extra_data";

    public static void show(Activity context, FileInfo info) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        DetailDialogFragment fragment = newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_DATA, info);
        fragment.setArguments(bundle);
        fragment.show(manager, FRAGMENT_TAG);
    }


    public DetailDialogFragment() {
    }

    public static DetailDialogFragment newInstance() {
        return new DetailDialogFragment();
    }

    private FileInfo info = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (null != arguments) {
            Parcelable parcelable = arguments.getParcelable(EXTRA_DATA);
            if (parcelable != null && parcelable instanceof FileInfo) {
                info = (FileInfo) parcelable;
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = null;
        if (null != info) {
            builder.setPositiveButton(R.string.submit, (dialog1, which) -> dialog1.dismiss());
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_detail, null, false);

            ImageView mTypeIv = view.findViewById(R.id.dialog_detail_type_iv);
            TextView mNameTv = view.findViewById(R.id.dialog_detail_name_tv);
            TextView mPathTv = view.findViewById(R.id.dialog_detail_path_tv);
            TextView mSizeTv = view.findViewById(R.id.dialog_detail_size_tv);
            TextView mLastTv = view.findViewById(R.id.dialog_detail_last_tv);
            TextView mHiddenTv = view.findViewById(R.id.dialog_detail_hidden_tv);
            TextView mPermissionTv = view.findViewById(R.id.dialog_detail_permission_tv);
            TextView mOwnerTv = view.findViewById(R.id.dialog_detail_owner_tv);
            TextView mGroupTv = view.findViewById(R.id.dialog_detail_group_tv);
            TextView mMimeTv = view.findViewById(R.id.dialog_detail_mime_tv);

            mTypeIv.setImageResource(info.getIcon());
            mNameTv.setText(info.getFileName());
            mPathTv.setText(info.getPath());
            mSizeTv.setText(info.getSize());
            mLastTv.setText(info.getLastModified());
            mHiddenTv.setText(info.isHiddenFile());
            mPermissionTv.setText(info.getPermission());
            mOwnerTv.setText(info.getOwner());
            mGroupTv.setText(info.getGroup());
            mMimeTv.setText(info.getMimeType());
            dialog = builder.create();
            dialog.setView(view);
            dialog.setCanceledOnTouchOutside(true);
        }
        return dialog;
    }

}
