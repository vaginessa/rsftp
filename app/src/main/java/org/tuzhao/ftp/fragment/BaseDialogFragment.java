package org.tuzhao.ftp.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * zhaotu
 * 17-8-7
 */
public class BaseDialogFragment extends DialogFragment {

    private OnDialogListener listener;

    public BaseDialogFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int style, theme;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            theme = android.R.style.Theme_Material_Dialog;
        } else {
            theme = android.R.style.Theme_Holo_Light_Dialog;
        }
        style = DialogFragment.STYLE_NO_TITLE;
        setStyle(style, theme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public final void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onDismiss();
        if (listener != null) listener.dismiss();
    }

    public void onDismiss() {

    }

    public boolean isShowing() {
        Dialog dialog = getDialog();
        return dialog != null && dialog.isShowing();
    }

    public void setOnDialogFragmentListener(OnDialogListener listener) {
        this.listener = listener;
    }

    public interface OnDialogListener {
        void dismiss();
    }

}
