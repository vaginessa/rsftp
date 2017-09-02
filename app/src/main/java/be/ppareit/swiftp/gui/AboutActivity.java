package be.ppareit.swiftp.gui;


import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import org.tuzhao.ftp.BuildConfig;
import org.tuzhao.ftp.R;
import org.tuzhao.ftp.activity.BaseActivity;

import java.util.List;

import be.ppareit.swiftp.FsSettings;


public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(FsSettings.getTheme());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

//        TextView packageNameText = (TextView) findViewById(R.id.about_package_name);
//        packageNameText.setText(BuildConfig.APPLICATION_ID + " (" + BuildConfig.FLAVOR + ")");

        TextView versionInfoText = (TextView) findViewById(R.id.about_version_info);
        versionInfoText.setText(BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE + " (" + Build.VERSION.RELEASE + "-" + Build.VERSION.SDK_INT + ")");

        TextView mLinkTv = (TextView) findViewById(R.id.about_link_tv);
        String url = getString(R.string.about_license_url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        List<ResolveInfo> info = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        if (info.size() > 0) {
            mLinkTv.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            Log.w("AboutActivity", "set auto link mask failure");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
