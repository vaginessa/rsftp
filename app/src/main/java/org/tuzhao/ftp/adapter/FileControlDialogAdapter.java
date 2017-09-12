package org.tuzhao.ftp.adapter;

import android.app.Activity;

import org.tuzhao.ftp.R;

/**
 * author: tuzhao
 * 2017-08-12 23:09
 */
public class FileControlDialogAdapter extends ServerControlDialogAdapter {

    public FileControlDialogAdapter(Activity context) {
        super(context);
    }

    @Override
    public String[] getMenu(Activity context) {
        return context.getResources().getStringArray(R.array.file_control_menu);
    }


}
