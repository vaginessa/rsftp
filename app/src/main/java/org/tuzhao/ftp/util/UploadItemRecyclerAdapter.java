package org.tuzhao.ftp.util;

import android.app.Activity;

import org.tuzhao.ftp.R;

/**
 * author: tuzhao
 * 2017-09-10 21:56
 */
public class UploadItemRecyclerAdapter extends DownloadItemRecyclerAdapter {

    public UploadItemRecyclerAdapter(Activity context) {
        super(context);
    }

    @Override
    protected String[] getStatusDesArray(Activity context) {
        return context.getResources().getStringArray(R.array.upload_item_status);
    }

}
