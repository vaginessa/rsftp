package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.util.BaseOnClickListener;

import java.io.File;
import java.io.FileOutputStream;

/**
 * author: tuzhao
 * 2017-08-30 21:02
 */
public class DonationDialogFragment extends BaseDialogFragment {

    private static final String FRAGMENT_TAG = "DonationDialogFragment";

    public static void show(Activity context) {
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        DonationDialogFragment fragment = newInstance();
        fragment.show(manager, FRAGMENT_TAG);
    }

    public static DonationDialogFragment newInstance() {
        return new DonationDialogFragment();
    }

    public DonationDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.donation, null, false);
        Button mSaveBt = view.findViewById(R.id.pay_save_bt);
        mSaveBt.setOnClickListener(new SaveImgClickListener());
        dialog.setView(view);
        return dialog;
    }

    private SaveImgTask task;

    private class SaveImgClickListener extends BaseOnClickListener {
        @Override
        public void click(View v) {
            task = new SaveImgTask();
            task.execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != task) {
            try {
                task.cancel(true);
            } catch (Exception e) {
                //...ignore...
            }
        }
        try {
            if (null != bitmapWechat)
                bitmapWechat.recycle();
        } catch (Exception e) {
            //...ignore...
        }
        try {
            if (null != bitmapAlipay)
                bitmapAlipay.recycle();
        } catch (Exception e) {
            //...ignore...
        }
    }

    private class SaveImgTask extends AsyncTask<String, Object, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return saveImage();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dismissLoadingDialog();
            if (result) {
                String des = getString(R.string.pay_save_successful);
                showMsg(String.format(des, dir));
            } else {
                showMsg(getString(R.string.pay_save_failure));
            }
        }
    }

    private String dir;
    private Bitmap bitmapWechat;
    private Bitmap bitmapAlipay;

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
        Bitmap bitmap = null;
        final Resources resources = getActivity().getResources();
        switch (ImageId) {
            case R.drawable.pay_alibaba:
                if (null == bitmapAlipay || bitmapAlipay.isRecycled()) {
                    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(ImageId);
                    bitmapAlipay = drawable.getBitmap();
                }
                bitmap = bitmapAlipay;
                break;
            case R.drawable.pay_wechat:
                if (null == bitmapWechat || bitmapWechat.isRecycled()) {
                    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(ImageId);
                    bitmapWechat = drawable.getBitmap();
                }
                bitmap = bitmapWechat;
                break;
        }
        if (null != bitmap) {
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            try {
                out.close();
            } catch (Exception e) {
                //...ignore...
            }
        } else {
            throw new RuntimeException("can't save a null bitmap");
        }
    }

    private SimpleDialogFragment dialogFragment;

    public void showLoadingDialog() {
        if (dialogFragment == null)
            dialogFragment = SimpleDialogFragment.newInstance();
        if (!dialogFragment.isShowing()) {
            dialogFragment.show(getFragmentManager(), "loadingDialogFragment");
        }
    }

    public void dismissLoadingDialog() {
        if (null != dialogFragment) {
            if (dialogFragment.isShowing()) {
                dialogFragment.dismiss();
            }
        }
    }

    public void log(String msg) {
        Log.d(FRAGMENT_TAG, msg);
    }
}
