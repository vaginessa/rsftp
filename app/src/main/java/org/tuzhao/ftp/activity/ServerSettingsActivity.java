package org.tuzhao.ftp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.db.RsDBHelper;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.fragment.ChooseDirFragment;

public class ServerSettingsActivity extends BaseActivity implements View.OnClickListener,
                                                                        ChooseDirFragment.OnSelectListener, AdapterView.OnItemSelectedListener {

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
    private EditText mPathEt;
    private EditText mAddressEt;
    private EditText mPortEt;
    private EditText mAccountEt;
    private EditText mPwdEt;
    private Spinner mSpinner;

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

        mAddressEt = (EditText) findViewById(R.id.server_settings_address_et);
        mPortEt = (EditText) findViewById(R.id.server_settings_port_et);
        mAccountEt = (EditText) findViewById(R.id.server_settings_account_et);
        mPwdEt = (EditText) findViewById(R.id.server_settings_pwd_et);
        mPathEt = (EditText) findViewById(R.id.server_settings_folder_et);
        TextView mOutTv = (TextView) findViewById(R.id.server_settings_out_bt);
        TextView mInTv = (TextView) findViewById(R.id.server_settings_in_bt);
        findViewById(R.id.server_settings_save_bt).setOnClickListener(this);
        mOutTv.setOnClickListener(this);
        mInTv.setOnClickListener(this);
        mSpinner = (Spinner) findViewById(R.id.server_settings_spinner);
        mSpinner.setOnItemSelectedListener(this);
        updateInterface();
    }

    private void updateInterface() {
        mAccountEt.setText(server.getAccount());
        mAddressEt.setText(server.getAddress());
        mPortEt.setText(server.getPort());
        mPwdEt.setText(server.getPwd());
        mPathEt.setText(server.getSavePath());
        mSpinner.setSelection(getEncodingPosition());
    }

    private int getEncodingPosition() {
        int position = 0;
        String[] array = getResources().getStringArray(R.array.server_encoding);
        for (int i = 0; i < array.length; i++) {
            if (server.getEncoding().equals(array[i])) {
                position = i;
                break;
            }
        }
        return position;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.server_settings_out_bt:
                if (isExternalStorageWritable()) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    ChooseDirFragment.show(getActivity(), path, this);
                } else {
                    showNoteDialog(getString(R.string.failed_external_storage));
                }
                break;
            case R.id.server_settings_in_bt:
                ChooseDirFragment.show(getActivity(), "", this);
                break;
            case R.id.server_settings_save_bt:
                saveServerInfo();
                break;
        }
    }

    private void saveServerInfo() {
        String account = mAccountEt.getText().toString().trim();
        String port = mPortEt.getText().toString().trim();
        String password = mPwdEt.getText().toString().trim();
        String address = mAddressEt.getText().toString().trim();
        String path = mPathEt.getText().toString().trim();
        if (null != server) {
            server.setPwd(password);
            server.setAccount(account);
            server.setAddress(address);
            server.setPort(port);
            server.setSavePath(path);
            int update = new RsDBHelper(getActivity()).updateServer(server);
            int stringId = R.string.server_info_save_failure;
            if (update != -1) {
                stringId = R.string.server_info_save_successful;
            }
            showSaveDialog(stringId);
        }
    }

    private void showSaveDialog(int stringId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.note);
        builder.setMessage(stringId);
        builder.setPositiveButton(R.string.submit, null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void onSelect(String path) {
        server.setSavePath(path);
        int update = new RsDBHelper(getActivity()).updateServer(server);
        if (update != -1) updateInterface();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        log("spinner select position: " + position);
        String[] array = getResources().getStringArray(R.array.server_encoding);
        if (position <= array.length) {
            String encoding = array[position];
            log("encoding: " + encoding);
            if (null != server) {
                server.setEncoding(encoding);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
