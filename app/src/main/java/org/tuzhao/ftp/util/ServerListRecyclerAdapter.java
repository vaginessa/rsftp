package org.tuzhao.ftp.util;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerEntity;

import java.util.List;

/**
 * author: tuzhao
 * 2017-08-12 10:44
 */
public class ServerListRecyclerAdapter extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener {

    private final Activity context;
    private final List<ServerEntity> list;

    public ServerListRecyclerAdapter(Activity context, List<ServerEntity> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_server_list, parent, false);
        inflate.setOnClickListener(this);
        inflate.setOnLongClickListener(this);
        return new ServerListViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ServerListViewHolder mHolder = (ServerListViewHolder) holder;
        mHolder.setViewTag(position);
        ServerEntity entity = list.get(position);
        mHolder.mNameTv.setText(entity.getAddress());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private static class ServerListViewHolder extends RecyclerView.ViewHolder {

        private View rootView;
        TextView mNameTv;

        ServerListViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            mNameTv = itemView.findViewById(R.id.item_server_tv);
        }

        void setViewTag(int position) {
            this.rootView.setTag(position);
        }
    }

    @Override
    public void onClick(View view) {
        if (null != itemClickListener) {
            int position = -1;
            Object tag = view.getTag();
            Object data = null;
            if (null != tag) {
                try {
                    position = Integer.parseInt(tag.toString());
                } catch (Exception e) {
                    //...ignore...
                }
            }
            if (-1 != position) {
                try {
                    data = list.get(position);
                } catch (Exception e) {
                    //...ignore...
                }
            }
            itemClickListener.onItemClick(view, data, position);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (null != itemLongClickListener) {
            int position = -1;
            Object tag = view.getTag();
            Object data = null;
            if (null != tag) {
                try {
                    position = Integer.parseInt(tag.toString());
                } catch (Exception e) {
                    //...ignore...
                }
            }
            if (-1 != position) {
                try {
                    data = list.get(position);
                } catch (Exception e) {
                    //...ignore...
                }
            }
            return itemLongClickListener.onItemLongClick(view, data, position);
        }
        return false;
    }

    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

}
