package org.tuzhao.ftp.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

/**
 * author: tuzhao
 * 2017-08-21 23:18
 */
public class SimpleRecyclerViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final View mItemView;
    private final SparseArray<View> mViews;

    public SimpleRecyclerViewHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
        this.mItemView = itemView;
        mViews = new SparseArray<>();
    }

    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mItemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public View itemView() {
        return mItemView;
    }

}
