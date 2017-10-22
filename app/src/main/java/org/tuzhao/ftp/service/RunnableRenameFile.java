package org.tuzhao.ftp.service;

import android.content.Context;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.util.System;
import org.tuzhao.ftp.util.WeakRunnable;

/**
 * author: tuzhao
 * 2017-08-14 23:56
 */
class RunnableRenameFile extends WeakRunnable<Context> {

    private final Object object = new Object();

    private ServerEntity server;
    private FTPClient client;
    private String oldName;
    private String serverPath;
    private String newName;


    RunnableRenameFile(Context context, ServerEntity server, String serverPath, String oldName, String newName) {
        super(context);
        this.server = server;
        this.oldName = oldName;
        this.serverPath = serverPath;
        this.newName = newName;
    }

    @Override
    public void weakRun(Context context) {
        boolean status;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final String address = server.getAddress();
        final int port = Integer.parseInt(server.getPort());
        final String account = server.getAccount();
        final String pwd = server.getPwd();
        final String encoding = server.getEncoding();
        log("encoding: " + encoding);

        boolean result = false;
        try {
            client = new FTPClient();
            client.setDefaultTimeout(15000);
            client.setConnectTimeout(15000);
            client.setListHiddenFiles(server.getDisplay() != 0);
            if (null != encoding && encoding.equals("UTF8")) {
                client.setAutodetectUTF8(true);
            }

            client.connect(address, port);

            status = client.login(account, pwd);
            client.setFileType(FTP.BINARY_FILE_TYPE);
            int bufferSize = client.getBufferSize();
            log("buffer size: " + bufferSize);
            log("connect result: " + status);

            client.changeWorkingDirectory(serverPath);

            result = client.rename(oldName, newName);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        log("rename result: " + result);
        System.sendServerRenameBroadcast(context, result);
    }

}
