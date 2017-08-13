package org.tuzhao.ftp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerEntity;

/**
 * zhaotu
 * 17-8-7
 */
public class ServerAddFragment extends BaseDialogFragment implements View.OnClickListener {

    private static final String FRAGMENT_TAG = "ServerAddFragment";
    private static final String TAG = "fragment";
    private static final String EXTRA_TYPE = "extra_type";
    private static final String EXTRA_DATA = "extra_data";
    /**
     * 添加server实例模式
     */
    public static final int START_TYPE_ADD = 0x10;
    /**
     * 修改server实例模式
     */
    public static final int START_TYPE_EDIT = 0x11;
    private int type;

    private EditText mServerAddress;
    private EditText mServerPort;
    private EditText mServerAccount;
    private EditText mServerPwd;

    private Button mServerSubmit;

    private String address;
    private String port;
    private String account;
    private String pwd;

    private OnCompleteListener listener;

    private ServerEntity server;

    public ServerAddFragment() {
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    public static void show(Activity context, ServerEntity obj, int TYPE, OnCompleteListener listener) {
        log("show type: " + TYPE + " obj is: " + obj);
        FragmentManager manager = context.getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragmentByTag = manager.findFragmentByTag(FRAGMENT_TAG);
        log("fragment by tag is " + (fragmentByTag == null ? "null" : "not null"));
        if (null != fragmentByTag) {
            transaction.remove(fragmentByTag);
        }
        transaction.addToBackStack(null);
        ServerAddFragment fragment = newInstance();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TYPE, TYPE);
        bundle.putParcelable(EXTRA_DATA, obj);
        fragment.setArguments(bundle);
        fragment.setOnCompleteListener(listener);
        if (!fragment.isShowing()) {
            fragment.show(manager, FRAGMENT_TAG);
        }
    }

    private static ServerAddFragment newInstance() {
        return new ServerAddFragment();
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");
        this.type = START_TYPE_ADD;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView");
        getDialog().setCanceledOnTouchOutside(false);
        View view = inflater.inflate(R.layout.server_add, container, false);
        mServerAddress = view.findViewById(R.id.server_add_address);
        mServerPort = view.findViewById(R.id.server_add_port);
        mServerAccount = view.findViewById(R.id.server_add_account);
        mServerPwd = view.findViewById(R.id.server_add_pwd);
        Button mServerCancel = view.findViewById(R.id.server_add_cancel);
        mServerSubmit = view.findViewById(R.id.server_add_submit);

        mServerCancel.setOnClickListener(this);
        mServerSubmit.setOnClickListener(this);

        Bundle bundle = getArguments();
        if (null != bundle) {
            this.type = bundle.getInt(EXTRA_TYPE);
            server = bundle.getParcelable(EXTRA_DATA);
            resetInterface(server);
        }
        if (server == null) server = new ServerEntity();
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.server_add_cancel:
                getDialog().dismiss();
                break;
            case R.id.server_add_submit:
                if (checkInput()) {
                    getDialog().dismiss();
                    if (null != listener) {
                        resetInterface(null);
                        server.setAccount(account);
                        server.setAddress(address);
                        server.setPort(port);
                        server.setPwd(pwd);
                        listener.onComplete(server, type);
                    }
                }
                break;
        }
    }

    private boolean checkInput() {
        address = mServerAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            showMsg(getString(R.string.address_note_one));
            return false;
        }

        port = mServerPort.getText().toString().trim();
        if (TextUtils.isEmpty(port)) {
            port = "2121";
        }
        account = mServerAccount.getText().toString().trim();
        pwd = mServerPwd.getText().toString().trim();
        return true;
    }

    private void resetInterface(ServerEntity entity) {
        if (null != mServerAddress)
            mServerAddress.setText(entity == null ? "" : entity.getAddress());
        if (null != mServerPort)
            mServerPort.setText(entity == null ? "" : entity.getPort());
        if (null != mServerAccount)
            mServerAccount.setText(entity == null ? "" : entity.getAccount());
        if (null != mServerPwd)
            mServerPwd.setText(entity == null ? "" : entity.getPwd());
        if (this.type == START_TYPE_EDIT) {
            if (null != mServerSubmit)
                mServerSubmit.setText(getString(R.string.save));
        }
        if (this.type == START_TYPE_ADD) {
            if (null != mServerSubmit)
                mServerSubmit.setText(getString(R.string.submit));
        }
    }

    public interface OnCompleteListener {
        void onComplete(ServerEntity server, int TYPE);
    }

}
