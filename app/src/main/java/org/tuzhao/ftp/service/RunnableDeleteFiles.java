package org.tuzhao.ftp.service;

import android.content.Context;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.entity.Status;
import org.tuzhao.ftp.fragment.DeleteDialogFragment;
import org.tuzhao.ftp.util.WeakRunnable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * author: tuzhao
 * 2017-08-14 23:56
 */
class RunnableDeleteFiles extends WeakRunnable<Context> {

    private final Object object = new Object();
    private boolean flag;

    private ServerEntity server;
    private FTPClient client;
    private ArrayList<RsFile> list;
    private String serverPath;

    private SimpleDateFormat format;

    RunnableDeleteFiles(Context context, ServerEntity server, ArrayList<RsFile> list, String serverPath) {
        super(context);
        this.server = server;
        this.flag = true;
        this.list = list;
        this.serverPath = serverPath;
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

        final String savePath = server.getSavePath();
        final int countTotal = list.size();

        for (int i = 0; i < countTotal; i++) {
            RsFile rsFile = list.get(i);
            String name = rsFile.getName();
            DeleteDialogFragment.sendStatusBroadCast(context, name, Status.DOING);
            boolean result;
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

                String fileServerPath = serverPath + "/" + name;
                result = client.deleteFile(fileServerPath);

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
                result = false;
            }
            if (result) {
                DeleteDialogFragment.sendStatusBroadCast(context, name, Status.SUCC);
            } else {
                DeleteDialogFragment.sendStatusBroadCast(context, name, Status.FAIL);
            }
        }
    }

    /**
     * this method run in son thread
     * @param path local file saved path
     * @return true can safe download, false can not download
     */
    private boolean localFileSafeCheck(String path) {
        boolean flag = true;
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent.exists()) {
            if (file.exists()) {
                String bk = path + getSuffix();
                flag = file.renameTo(new File(bk));
                log("file: " + path + " rename to: " + bk + " result: " + flag);
            }
        } else {
            flag = parent.mkdirs();
            log("mkdirs " + parent.getAbsolutePath() + " result: " + flag);
        }
        return flag;
    }

    private String getSuffix() {
        if (null == format) {
            format = new SimpleDateFormat("yy_MM_dd_HH_mm_ss");
        }
        String str = format.format(new Date());
        return "_bk_" + str;
    }

}
