package org.tuzhao.ftp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.fragment.SimpleDialogFragment;

/**
 * zhaotu
 * 17-7-31
 */
public class BaseActivity extends AppCompatActivity {

    private Handler defaultHandler;

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public Activity getActivity() {
        return this;
    }

    public Handler getDefaultHandler() {
        if (null == defaultHandler)
            defaultHandler = new Handler(this.getMainLooper());
        return defaultHandler;
    }

    public void showMsg(String msg) {
        showMsg(msg, Toast.LENGTH_LONG);
    }

    public void showMsg(String msg, int length) {
        Toast.makeText(this, msg, length).show();
    }

    public void log(String msg) {
        Log.d(this.getClass().getSimpleName(), msg);
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

    public void showNoteDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.note);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.submit, null);
        builder.create().show();
    }

}
