package org.tuzhao.ftp.activity;

import android.content.Intent;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;

import org.tuzhao.ftp.R;

import be.ppareit.swiftp.gui.MainActivity;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        MobclickAgent.setDebugMode(true);
        MobclickAgent.enableEncrypt(true);

        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            WelcomeActivity.this.finish();
        }, 3000);
    }

}
