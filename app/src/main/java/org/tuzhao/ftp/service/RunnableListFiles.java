package org.tuzhao.ftp.service;

import android.content.Context;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.tuzhao.ftp.activity.ServerItemActivity;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.util.WeakRunnable;

import java.util.ArrayList;
import java.util.Collections;

/**
 * author: tuzhao
 * 2017-08-14 23:56
 */

class RunnableListFiles extends WeakRunnable<Context> {

    private ServerEntity server;

    RunnableListFiles(Context context, ServerEntity server) {
        super(context);
        this.server = server;
    }

    @Override
    public void weakRun(Context context) {
        boolean status = false;
        FTPClient client;
        try {
            String address = server.getAddress();
            int port = Integer.parseInt(server.getPort());
            String account = server.getAccount();
            String pwd = server.getPwd();
            client = new FTPClient();
            client.setAutodetectUTF8(true);
            client.connect(address, port);
            status = client.login(account, pwd);
            log("connect result: " + status);
        } catch (Exception e) {
            client = null;
            e.printStackTrace();
        }
        FTPFile[] array = null;
        try {
            if (status && null != client) {
                array = client.listFiles();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<FTPFile> list = null;
        if (array != null) {
            list = new ArrayList<>();
            Collections.addAll(list, array);
        }
        ServerItemActivity.sendListFilesResult(context, list);
        try {
            if (null != client) {
                client.abort();
            }
        } catch (Exception e) {
            //...ignore...
        }
        try {
            if (null != client) {
                client.disconnect();
            }
        } catch (Exception e) {
            //...ignore...
        }
        client = null;
    }

}
