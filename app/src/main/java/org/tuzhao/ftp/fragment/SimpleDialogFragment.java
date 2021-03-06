package org.tuzhao.ftp.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tuzhao.ftp.R;


/**
 * 显示一个简单进度条的DialogFragment
 * @author tuzhao
 */
public class SimpleDialogFragment extends BaseDialogFragment {

    private OnDialogListener listener;

    public SimpleDialogFragment() {
        // Required empty public constructor
    }

    public static SimpleDialogFragment newInstance() {
        return new SimpleDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.loading_layout, container, false);
    }

}
