package org.tuzhao.ftp.util;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.DownLoadEntity;

import java.util.ArrayList;

/**
 * author: tuzhao
 * 2017-09-09 11:00
 */
public class DownloadItemRecyclerAdapter extends RecyclerView.Adapter {

    private final Activity context;
    private final ArrayList<DownLoadEntity> list = new ArrayList<>();
    private final String[] array;

    public DownloadItemRecyclerAdapter(Activity context) {
        this.context = context;
        array = getStatusDesArray(context);
    }

    protected String[] getStatusDesArray(Activity context) {
        return context.getResources().getStringArray(R.array.download_item_status);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_download_status, parent, false);
        return new DownloadItemViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DownloadItemViewHolder viewHolder = (DownloadItemViewHolder) holder;
        DownLoadEntity entity = list.get(position);
        viewHolder.mServerSizeTv.setText(entity.getFileName());
        viewHolder.mServerTimeTv.setText(array[entity.getStatus()]);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void update(String name, int status) {
        boolean flag = false;
        for (int i = 0; i < list.size(); i++) {
            DownLoadEntity entity = list.get(i);
            if (entity.getFileName().equals(name)) {
                entity.setStatus(status);
                flag = true;
            }
        }
        if (!flag) {
            DownLoadEntity entity = new DownLoadEntity();
            entity.setStatus(status);
            entity.setFileName(name);
            list.add(entity);
        }
        notifyDataSetChanged();
    }

    private static class DownloadItemViewHolder extends RecyclerView.ViewHolder {

        View item;
        TextView mServerSizeTv;
        TextView mServerTimeTv;

        DownloadItemViewHolder(View itemView) {
            super(itemView);
            this.item = itemView;
            mServerSizeTv = item.findViewById(R.id.item_download_name_tv);
            mServerTimeTv = item.findViewById(R.id.item_download_status_tv);
        }
    }

}
