package org.tuzhao.ftp.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.tuzhao.ftp.R;

/**
 * author: tuzhao
 * 2017-08-12 23:09
 */
public class ServerControlDialogAdapter extends BaseAdapter {

    private Activity context;
    private String[] menu;

    public ServerControlDialogAdapter(Activity context) {
        this.context = context;
        menu = getMenu(context);
    }

    public String[] getMenu(Activity context) {
        return context.getResources().getStringArray(R.array.server_control_menu);
    }

    @Override
    public int getCount() {
        return menu.length;
    }

    @Override
    public Object getItem(int i) {
        return menu[i];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView mMenuTv;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_server_control, null);
            mMenuTv = view.findViewById(R.id.item_server_control_tv);
            ViewHolder viewHolder = new ViewHolder(mMenuTv);
            view.setTag(viewHolder);
        } else {
            ViewHolder holder = (ViewHolder) view.getTag();
            mMenuTv = holder.mMenuTv;
        }
        mMenuTv.setText(menu[i]);
        return view;
    }

    private static class ViewHolder {
        TextView mMenuTv;

        ViewHolder(TextView mMenuTv) {
            this.mMenuTv = mMenuTv;
        }

    }

}
