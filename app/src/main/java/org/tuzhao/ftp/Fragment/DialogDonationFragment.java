package org.tuzhao.ftp.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.tuzhao.ftp.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * author: tuzhao
 * 2017-08-30 21:02
 */
public class DialogDonationFragment extends DialogFragment implements View.OnClickListener {

    private static final String FRAGMENT_TAG = "DialogDonationFragment";

    public static void show(Activity context) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        DialogDonationFragment fragment = newInstance();
        fragment.show(manager, FRAGMENT_TAG);
    }

    public static DialogDonationFragment newInstance() {
        return new DialogDonationFragment();
    }

    public DialogDonationFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.donation, null, false);
        Button mSaveBt = view.findViewById(R.id.pay_save_bt);
        mSaveBt.setOnClickListener(this);
        dialog.setView(view);
        return dialog;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pay_save_bt:
                if (saveImage()) {
                    String des = getString(R.string.pay_save_successful);
                    showMsg(String.format(des, dir));
                } else {
                    showMsg(getString(R.string.pay_save_failure));
                }
                break;
        }
    }

    private void showMsg(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private String dir;

    private boolean saveImage() {
        boolean flag = false;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                dir = Environment.getExternalStorageDirectory().getAbsolutePath();
                dir = dir + "/rsftp";
                log(dir);
                File file = new File(dir);
                if (!file.exists() || !file.isDirectory()) {
                    boolean mkdirs = file.mkdirs();
                    log("create dir result: " + mkdirs);
                }
                String weChat = dir + "/pay_wechat.png";
                String alipay = dir + "/pay_alipay.png";

                int idWechat = R.drawable.pay_wechat;
                int idAlipay = R.drawable.pay_alibaba;

                saveImage(weChat, idWechat);
                saveImage(alipay, idAlipay);

                updateGallery(weChat, alipay);

                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    private void updateGallery(String... args) {
        for (String path : args) {
            try {
                File file = new File(path);
                String name = file.getName();
                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), path, name, name);
                Uri uri = Uri.fromFile(file);
                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage(String path, int ImageId) throws Exception {
        final Resources resources = getActivity().getResources();
        BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(ImageId);
        Bitmap bitmap = drawable.getBitmap();
        FileOutputStream out = new FileOutputStream(path);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
            out.close();
        } catch (Exception e) {
            //...ignore...
        }
        try {
            bitmap.recycle();
        } catch (Exception e) {
            ///...ignore...
        }
    }

    private void log(String msg) {
        Log.d(FRAGMENT_TAG, msg);
    }
}
