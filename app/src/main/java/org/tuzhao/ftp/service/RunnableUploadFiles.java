package org.tuzhao.ftp.service;

import android.content.Context;

import org.apache.commons.net.ftp.FTPClient;
import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.RsLocalFile;
import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.util.WeakRunnable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * author: tuzhao
 * 2017-08-14 23:56
 */
class RunnableUploadFiles extends WeakRunnable<Context> {

    private final Object object = new Object();
    private boolean flag;

    private ServerEntity server;
    private FTPClient client;
    private ArrayList<RsFile> list;
    private String serverPath;

    private SimpleDateFormat format;

    RunnableUploadFiles(Context context, ServerEntity server, ArrayList<RsFile> list, String serverPath) {
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

        final int countTotal = list.size();

        for (int i = 0; i < countTotal; i++) {
            RsFile rsFile = list.get(i);
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
//                client.setFileType(FTP.BINARY_FILE_TYPE);
                int bufferSize = client.getBufferSize();
                log("buffer size: " + bufferSize);

                log("connect result: " + status);

                String path = ((RsLocalFile) rsFile).getAbsolutePath();
                if (localFileSafeCheck(path)) {
                    byte[] bytes = new byte[512];
                    int read;
                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
                    OutputStream out = client.storeFileStream(rsFile.getName());
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
                    log("index[" + i + "]upload file: " + path + " result: true");
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private boolean localFileSafeCheck(String path) {
        File file = new File(path);
        return file.isFile() && file.exists();
    }

}
