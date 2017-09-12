package org.tuzhao.ftp.adapter;

import android.app.Activity;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.RsFTPFile;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.RsLocalFile;
import org.tuzhao.ftp.fragment.SimpleRecyclerViewHolder;
import org.tuzhao.ftp.util.FileType;
import org.tuzhao.ftp.util.OnItemClickListener;
import org.tuzhao.ftp.util.OnItemLongClickListener;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * author: tuzhao
 * 2017-08-15 19:48
 */
public class ServerItemRecyclerAdapter extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener {

    private static final int TYPE_HEADER = 0x1000;
    private static final int TYPE_FOOTER = 0x2000;
    private static final int TYPE_NORMAL = 0x3000;

    private Activity context;
    private ArrayList<RsFile> list;

    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFootViews = new SparseArrayCompat<>();

    public ServerItemRecyclerAdapter(Activity context, ArrayList<RsFile> list) {
        this.context = context;
        this.list = list;
    }

    public void setHeaderView(View headerView) {
        this.mHeaderViews.put(mHeaderViews.size() + TYPE_HEADER, headerView);
    }

    public void setFooterView(View footerView) {
        this.mFootViews.put(mFootViews.size() + TYPE_FOOTER, footerView);
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public int getFootersCount() {
        return mFootViews.size();
    }

    private boolean isHeaderViewPos(int position) {
        return position < getHeadersCount();
    }

    private boolean isFooterViewPos(int position) {
        return position >= getHeadersCount() + getRealItemCount();
    }

    private int getRealItemCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterViewPos(position)) {
            return mFootViews.keyAt(position);
        }
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null != mHeaderViews.get(viewType)) {
            return new SimpleRecyclerViewHolder(context, mHeaderViews.get(viewType));
        } else if (null != mFootViews.get(viewType)) {
            return new SimpleRecyclerViewHolder(context, mFootViews.get(viewType));
        }
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_server_item, parent, false);
        inflate.setOnClickListener(this);
        inflate.setOnLongClickListener(this);
        return new ServerItemViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderViewPos(position)) return;
        if (isFooterViewPos(position)) return;

        position = position - getHeadersCount();
        ServerItemViewHolder viewHolder = (ServerItemViewHolder) holder;
        viewHolder.setViewTag(position);

        RsFile file = list.get(position);
        String name = file.getName();
        long timeInMillis = file.getModifyTimeMillis();

        viewHolder.mServerTypeIv.setImageResource(FileType.getFileDesImg(file));
        viewHolder.mServerTypeTv.setText(name);
        viewHolder.mServerTimeTv.setText(getDate(timeInMillis));
        viewHolder.mSelectedIv.setVisibility(isSelected(file) ? View.VISIBLE : View.INVISIBLE);
        if (file.isDir()) {
            viewHolder.mServerSizeTv.setVisibility(View.GONE);
        } else {
            long size = file.getSize();
            viewHolder.mServerSizeTv.setText(getSize(size));
            viewHolder.mServerSizeTv.setVisibility(View.VISIBLE);
        }
    }

    private boolean isSelected(RsFile rsFile) {
        boolean flag = false;
        if (null != rsFile && (rsFile instanceof RsFTPFile || rsFile instanceof RsLocalFile)) {
            flag = rsFile.getSelected();
        }
        return flag;
    }

    @Override
    public int getItemCount() {
        return getRealItemCount() + getHeadersCount() + getFootersCount();
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

    private static class ServerItemViewHolder extends RecyclerView.ViewHolder {

        View item;
        ImageView mServerTypeIv;
        ImageView mSelectedIv;
        TextView mServerTypeTv;
        TextView mServerSizeTv;
        TextView mServerTimeTv;

        ServerItemViewHolder(View itemView) {
            super(itemView);
            this.item = itemView;
            mSelectedIv = item.findViewById(R.id.item_server_selected_iv);
            mServerTypeIv = item.findViewById(R.id.item_server_type_iv);
            mServerTypeTv = item.findViewById(R.id.item_server_type_tv);
            mServerSizeTv = item.findViewById(R.id.item_server_size_tv);
            mServerTimeTv = item.findViewById(R.id.item_server_time_tv);
        }

        void setViewTag(int position) {
            this.item.setTag(position);
        }
    }

    public static String getSize(long size) {
        float base = 1024F;
        String type = " KB";
        float result = size / base;
        if (result > base) {
            result /= base;
            type = " MB";
        }
        if (result > base) {
            result /= base;
            type = " GB";
        }
        return getNum(result) + type;
    }

    private static String getNum(float num) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.UP);
        return nf.format(num);
    }

    private static SimpleDateFormat format;

    static String getDate(long time) {
        if (null == format)
            format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        return format.format(new Date(time));
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
