package org.tuzhao.ftp.service;

import android.content.Context;
import android.text.TextUtils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.tuzhao.ftp.activity.ServerItemActivity;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.util.System;
import org.tuzhao.ftp.util.WeakRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * author: tuzhao
 * 2017-08-14 23:56
 */

class RunnableListFiles extends WeakRunnable<Context> {

    private ServerEntity server;
    private String path;

    RunnableListFiles(Context context, ServerEntity server, String path) {
        super(context);
        this.server = server;
        this.path = path;
    }

    @Override
    public void weakRun(Context context) {
        boolean status;
        FTPClient client;
        String address = server.getAddress();
        int port = Integer.parseInt(server.getPort());
        String account = server.getAccount();
        String pwd = server.getPwd();
        client = new FTPClient();
        client.setDefaultTimeout(15000);
        client.setAutodetectUTF8(true);

        try {
            client.connect(address, port);
        } catch (IOException e) {
            e.printStackTrace();
            System.sendServerConnectExceptionBroadcast(context, e.getMessage());
            return;
        }

        try {
            status = client.login(account, pwd);
        } catch (IOException e) {
            e.printStackTrace();
            System.sendServerLoginExceptionBroadcast(context, e.getMessage());
            return;
        }

        log("connect result: " + status);
        if (!status) {
            System.sendServerLoginFailed(context);
        }

        try {
            log("list files path: " + path);
            if (!(path == null || TextUtils.isEmpty(path))) {
                client.changeWorkingDirectory(path);
            }
            String directory = client.printWorkingDirectory();
            log("current path: " + directory);
            System.sendServerCurrentPathBroadcast(context, directory);
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
