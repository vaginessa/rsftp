package org.tuzhao.ftp.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

/**
 * zhaotu
 * 17-7-31
 */
public class BaseActivity extends AppCompatActivity {

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

    public void showMsg(String msg) {
        showMsg(msg, Toast.LENGTH_LONG);
    }

    public void showMsg(String msg, int length) {
        Toast.makeText(this, msg, length).show();
    }

    public void log(String msg) {
        Log.d(this.getClass().getSimpleName(), msg);
    }

}
