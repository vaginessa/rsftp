package org.tuzhao.ftp.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.tuzhao.ftp.R;
import org.tuzhao.ftp.entity.ServerAddItem;

/**
 * zhaotu
 * 17-8-7
 */
public class ServerAddFragment extends BaseDialogFragment implements View.OnClickListener {

    private EditText mServerAddress;
    private EditText mServerPort;
    private EditText mServerAccount;
    private EditText mServerPwd;

    private String address;
    private String port;
    private String account;
    private String pwd;

    private OnCompleteListener listener;

    public ServerAddFragment() {
    }

    public static ServerAddFragment newInstance() {
        return new ServerAddFragment();
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        View view = inflater.inflate(R.layout.server_add, container, false);
        mServerAddress = view.findViewById(R.id.server_add_address);
        mServerPort = view.findViewById(R.id.server_add_port);
        mServerAccount = view.findViewById(R.id.server_add_account);
        mServerPwd = view.findViewById(R.id.server_add_pwd);
        Button mServerCancel = view.findViewById(R.id.server_add_cancel);
        Button mServerSubmit = view.findViewById(R.id.server_add_submit);

        mServerCancel.setOnClickListener(this);
        mServerSubmit.setOnClickListener(this);
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
                        ServerAddItem server = new ServerAddItem();
                        server.setAccount(account);
                        server.setAddress(address);
                        server.setPort(port);
                        server.setPwd(pwd);
                        listener.onComplete(server);
                    }
                }
                break;
        }
    }

    private boolean checkInput() {
        address = mServerAddress.getText().toString().trim();
        port = mServerPort.getText().toString().trim();
        account = mServerAccount.getText().toString().trim();
        pwd = mServerPwd.getText().toString().trim();
        return true;
    }

    public interface OnCompleteListener {
        void onComplete(ServerAddItem server);
    }

}
