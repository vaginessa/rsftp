package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.util.System;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.tuzhao.ftp.util.System.SHARED_CONFIG_FILE;
import static org.tuzhao.ftp.util.System.SHARED_WIFI_KEY;

/**
 * author: tuzhao
 * 2017-09-03 13:54
 */
public class WifiDialogFragment extends BaseDialogFragment {

    private static final String FRAGMENT_TAG = "WifiDialogFragment";
    private final ArrayList<String> choose = new ArrayList<>();
    private ArrayList<String> total = new ArrayList<>();
    private String[] array;
    private boolean[] select;

    public static void show(Activity context) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        WifiDialogFragment fragment = new WifiDialogFragment();
        fragment.show(manager, FRAGMENT_TAG);
    }

    public WifiDialogFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Context context = getActivity().getApplicationContext();
            SharedPreferences share = context.getSharedPreferences(SHARED_CONFIG_FILE, Context.MODE_PRIVATE);
            String str = share.getString(SHARED_WIFI_KEY, "");
            ArrayList<String> save = System.stringToList(str);
            choose.addAll(save);

            WifiManager wifiManager = (WifiManager)
                                          getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<WifiConfiguration> configs = null;
            if (null != wifiManager) {
                configs = wifiManager.getConfiguredNetworks();
            }
            ArrayList<String> current = new ArrayList<>();
            if (null != configs) {
                for (int i = 0; i < configs.size(); ++i) {
                    String name = configs.get(i).SSID;
                    if (name.length() > 2 && name.startsWith("\"") && name.endsWith("\"")) {
                        name = name.substring(1, name.length() - 1);
                    }
                    current.add(name);
                }
            }

            ArrayList<String> all = new ArrayList<>();
            all.addAll(save);
            for (int i = 0; i < current.size(); i++) {
                if (!all.contains(current.get(i))) {
                    all.add(current.get(i));
                }
            }

            Collections.sort(all, new WifiNameComparator(save));

            array = new String[all.size()];
            for (int i = 0; i < all.size(); i++) {
                array[i] = all.get(i);
            }

            select = new boolean[all.size()];
            for (int i = 0; i < all.size(); i++) {
                String s = all.get(i);
                select[i] = save.contains(s);
            }
            total.addAll(all);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.autoconnect_label);
        if (total.size() > 0 && (null != array && null != select)) {
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.submit, (dialogInterface, i) -> {
                SharedPreferences share = getActivity().
                                                           getApplicationContext().getSharedPreferences(SHARED_CONFIG_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = share.edit();
                edit.putString(SHARED_WIFI_KEY, System.listToString(choose));
                edit.apply();
            });
            builder.setMultiChoiceItems(array, select, (dialogInterface, position, flag) -> {
                final String item = total.get(position);
                log("p: " + position + " flag: " + flag + " item: " + item);
                if (flag) {
                    if (!choose.contains(item)) {
                        choose.add(item);
                    }
                } else {
                    if (choose.contains(item)) {
                        choose.remove(item);
                    }
                }
            });
        } else {
            builder.setMessage(R.string.auto_connect_note);
            builder.setPositiveButton(R.string.submit, null);
        }
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private static class WifiNameComparator implements Comparator<String> {

        private ArrayList<String> list;

        WifiNameComparator(ArrayList<String> list) {
            this.list = list;
        }

        @Override
        public int compare(String s1, String s2) {
            boolean c1 = list.contains(s1);
            boolean c2 = list.contains(s2);
            if (c1) {
                if (c2) {
                    return s1.compareTo(s2);
                } else {
                    return -1;
                }
            }
            if (c2) {
                return 1;
            }
            return s1.compareTo(s2);
        }

    }

}
