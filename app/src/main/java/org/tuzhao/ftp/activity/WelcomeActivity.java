package org.tuzhao.ftp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.util.Umeng;
import org.tuzhao.ftp.util.WeakRunnable;

import be.ppareit.swiftp.gui.MainActivity;

public class WelcomeActivity extends BaseActivity {

    private static final String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        MobclickAgent.setDebugMode(true);
        MobclickAgent.enableEncrypt(true);

        String deviceInfo = Umeng.getDeviceInfo(getActivity());
        Log.i(TAG, "device info: " + deviceInfo);

        new android.os.Handler().postDelayed(new WelcomeRunnable(this), 3000);
    }

    private static class WelcomeRunnable extends WeakRunnable<WelcomeActivity> {

        WelcomeRunnable(WelcomeActivity context) {
            super(context);
        }

        @Override
        public void weakRun(WelcomeActivity context) {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
            context.finish();
        }
    }

}
