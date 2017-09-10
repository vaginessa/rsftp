package org.tuzhao.ftp.service;

import android.content.Context;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.entity.Status;
import org.tuzhao.ftp.fragment.DownloadDialogFragment;
import org.tuzhao.ftp.util.WeakRunnable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * author: tuzhao
 * 2017-08-14 23:56
 */
class RunnableDownloadFiles extends WeakRunnable<Context> {

    private final Object object = new Object();
    private boolean flag;

    private ServerEntity server;
    private FTPClient client;
    private ArrayList<RsFile> list;
    private String serverPath;

    private SimpleDateFormat format;

    RunnableDownloadFiles(Context context, ServerEntity server, ArrayList<RsFile> list, String serverPath) {
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
        String address = server.getAddress();
        int port = Integer.parseInt(server.getPort());
        String account = server.getAccount();
        String pwd = server.getPwd();
        String encoding = server.getEncoding();
        log("encoding: " + encoding);

        final String savePath = server.getSavePath();
        final int countTotal = list.size();

        for (int i = 0; i < countTotal; i++) {
            RsFile rsFile = list.get(i);
            String name = rsFile.getName();
            DownloadDialogFragment.sendStatusBroadCast(context, name, Status.DOING);
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

                boolean result;

                result = false;
                String fileServerPath = serverPath + "/" + name;
                String fileLocalPath = savePath + "/" + name;
                if (localFileSafeCheck(fileLocalPath)) {
                    byte[] bytes = new byte[512];
                    int read;
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileLocalPath));
                    InputStream in = client.retrieveFileStream(fileServerPath);
                    while ((read = in.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                    out.flush();
                    try {
                        out.close();
                    } catch (Exception e) {
                        //...ignore...
                    }
                    try {
                        in.close();
                    } catch (Exception e) {
                        //...ignore...
                    }
                    result = true;
                    log("index[" + i + "]download file: " + fileServerPath + " result: true");
                }

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
                DownloadDialogFragment.sendStatusBroadCast(context, name, Status.SUCC);
            } catch (Exception e) {
                DownloadDialogFragment.sendStatusBroadCast(context, name, Status.FAIL);
                e.printStackTrace();
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
