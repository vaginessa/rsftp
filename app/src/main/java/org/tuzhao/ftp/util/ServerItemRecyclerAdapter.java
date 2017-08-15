package org.tuzhao.ftp.util;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;
import org.tuzhao.ftp.R;

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

    private Activity context;
    private ArrayList<FTPFile> list;

    public ServerItemRecyclerAdapter(Activity context, ArrayList<FTPFile> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_server_item, parent, false);
        inflate.setOnClickListener(this);
        inflate.setOnLongClickListener(this);
        return new ServerItemViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ServerItemViewHolder viewHolder = (ServerItemViewHolder) holder;
        viewHolder.setViewTag(position);

        FTPFile ftpFile = list.get(position);
        String name = ftpFile.getName();
        long timeInMillis = ftpFile.getTimestamp().getTimeInMillis();

        viewHolder.mServerTypeIv.setImageResource(getFileDesImg(ftpFile));
        viewHolder.mServerTypeTv.setText(name);
        viewHolder.mServerTimeTv.setText(getDate(timeInMillis));
        if (ftpFile.isDirectory()) {
            viewHolder.mServerSizeTv.setVisibility(View.GONE);
        } else {
            long size = ftpFile.getSize();
            viewHolder.mServerSizeTv.setText(getSize(size));
            viewHolder.mServerSizeTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
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
        TextView mServerTypeTv;
        TextView mServerSizeTv;
        TextView mServerTimeTv;

        ServerItemViewHolder(View itemView) {
            super(itemView);
            this.item = itemView;
            mServerTypeIv = item.findViewById(R.id.item_server_type_iv);
            mServerTypeTv = item.findViewById(R.id.item_server_type_tv);
            mServerSizeTv = item.findViewById(R.id.item_server_size_tv);
            mServerTimeTv = item.findViewById(R.id.item_server_time_tv);
        }

        void setViewTag(int position) {
            this.item.setTag(position);
        }
    }

    private static int getFileDesImg(FTPFile file) {
        int img = R.drawable.file_unknown;
        if (file.isDirectory()) {
            img = R.drawable.folder;
        } else if (file.isFile()) {
            switch (getFileType(file.getName())) {
                case ".jpg":
                    img = R.drawable.file_image;
                    break;
                case ".pdf":
                    img = R.drawable.file_pdf;
                    break;
                case ".png":
                    img = R.drawable.file_image;
                    break;
                case ".txt":
                    img = R.drawable.file_txt;
                    break;
                case ".xml":
                    img = R.drawable.file_xml;
                    break;
                case ".doc":
                    img = R.drawable.file_doc;
                    break;
                case ".ppt":
                    img = R.drawable.file_ppt;
                    break;
                default:
                    img = R.drawable.file_file;
                    break;
            }
        }
        return img;
    }

    private static String getFileType(String name) {
        String type = "unknown";
        try {
            int lastIndexOf = name.lastIndexOf(".");
            type = name.substring(lastIndexOf, name.length());
        } catch (Exception e) {
            //...ignore...
        }
        return type;
    }

    private static String getSize(long size) {
        String type = " KB";
        float result = size / 1024F;
        if (result > 1024F) {
            result /= 1024F;
            type = " MB";
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
