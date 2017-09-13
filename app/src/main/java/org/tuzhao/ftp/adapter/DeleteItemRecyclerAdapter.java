package org.tuzhao.ftp.adapter;

import android.app.Activity;

import org.tuzhao.ftp.R;

/**
 * author: tuzhao
 * 2017-09-10 21:56
 */
public class DeleteItemRecyclerAdapter extends DownloadItemRecyclerAdapter {

    public DeleteItemRecyclerAdapter(Activity context) {
        super(context);
    }

    @Override
    protected String[] getStatusDesArray(Activity context) {
        return context.getResources().getStringArray(R.array.delete_item_status);
    }

}
