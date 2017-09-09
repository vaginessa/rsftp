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

    private final Object object = new Object();

    private ServerEntity server;
    private String path;
    private FTPClient client;
    private boolean flag;

    RunnableListFiles(Context context, ServerEntity server, String path) {
        super(context);
        this.server = server;
        this.path = path;
        this.flag = true;
    }

    void taskClose() {
        log("task close start");
        setFlag(false);
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
        log("task close end");
    }

    private void setFlag(boolean flag) {
        synchronized (object) {
            this.flag = flag;
            log("task process flag set: " + this.flag);
        }
    }

    private boolean getFlag() {
        synchronized (object) {
            log("task process flag get : " + flag);
            return flag;
        }
    }

    @Override
    public void weakRun(Context context) {
        boolean status;
        String address = server.getAddress();
        int port = Integer.parseInt(server.getPort());
        String account = server.getAccount();
        String pwd = server.getPwd();
        String encoding = server.getEncoding();
        log("encoding: " + encoding);
        client = new FTPClient();
        client.setDefaultTimeout(15000);
        client.setConnectTimeout(15000);
        client.setListHiddenFiles(server.getDisplay() != 0);
        if (null != encoding && encoding.equals("UTF8")) {
            client.setAutodetectUTF8(true);
        }
        try {
            client.connect(address, port);
        } catch (IOException e) {
            e.printStackTrace();
            if (getFlag()) {
                System.sendServerConnectExceptionBroadcast(context, e.getMessage());
            }
            return;
        }

        try {
            status = client.login(account, pwd);
        } catch (IOException e) {
            e.printStackTrace();
            if (getFlag()) {
                System.sendServerLoginExceptionBroadcast(context, e.getMessage());
            }
            return;
        }

        log("connect result: " + status);
        if (!status) {
            if (getFlag()) {
                System.sendServerLoginFailed(context);
            }
        }

        String directory = null;
        try {
            log("list files path: " + path);
            if (!(path == null || TextUtils.isEmpty(path))) {
                client.changeWorkingDirectory(path);
            }
            directory = client.printWorkingDirectory();
            log("current path: " + directory);
            if (getFlag()) {
                System.sendServerCurrentPathBroadcast(context, directory);
            }
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
        if (getFlag()) {
            ServerItemActivity.sendListFilesResult(context, list);
        }

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
