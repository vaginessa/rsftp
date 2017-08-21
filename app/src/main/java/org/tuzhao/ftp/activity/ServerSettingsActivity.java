package org.tuzhao.ftp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.fragment.ChooseDirFragment;

import java.io.File;

public class ServerSettingsActivity extends BaseActivity implements View.OnClickListener {

    private static final String EXTRA_SERVER = "extra_server";

    public static void start(Activity context, Parcelable parcelable) {

        if (!(parcelable instanceof ServerEntity)) {
            throw new RuntimeException("must give me a ServerEntity Instance!");
        }
        Intent intent = new Intent(context, ServerSettingsActivity.class);
        intent.putExtra(EXTRA_SERVER, parcelable);
        context.startActivity(intent);
    }

    private ServerEntity server;

    private TextView mOutTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        Parcelable parcelable = getIntent().getParcelableExtra(EXTRA_SERVER);
        if (null != parcelable && !(parcelable instanceof ServerEntity)) {
            showMsg("receive server info error");
            finish();
            return;
        } else {
            server = (ServerEntity) parcelable;
            log("start server info: " + server);
        }


        mOutTv = (TextView) findViewById(R.id.server_settings_out_bt);
        mOutTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.server_settings_out_bt:
                File cacheDir = getCacheDir();
                String path = cacheDir.getAbsolutePath();
                log("cache path:" + path);

                File filesDir = getFilesDir();
                path = filesDir.getAbsolutePath();
                log("files dir path:" + path);

                ChooseDirFragment.show(getActivity());
                break;
        }
    }
}
