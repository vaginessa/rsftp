package org.tuzhao.ftp.util;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * zhaotu
 * 17-8-7
 */
public class ServerClient {

    private static final String TAG = "ServerClient";

    private FTPClient mFtpClient = null;

    public ServerClient() {
        mFtpClient = new FTPClient();
        this.mFtpClient.setConnectTimeout(10 * 1000);
    }

    public void setFtpClient(FTPClient mFtpClient) {
        this.mFtpClient = mFtpClient;
    }

    public void useCompressedTransfer() {
        try {
            mFtpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.COMPRESSED_TRANSFER_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ;
    }

    public String[] listName() throws Exception {
        try {
            return mFtpClient.listNames();
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean setWorkingDirectory(String dir) throws Exception {
        try {
            return mFtpClient.changeWorkingDirectory(dir);
        } catch (Exception e) {
            throw e;
        }
    }

    public FTPClient getFtpClient() {
        return mFtpClient;
    }

    public void setTimeout(int seconds) throws Exception {
        try {
            mFtpClient.setConnectTimeout(seconds * 1000);
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean makeDir(String dir) throws Exception {
        try {
            return mFtpClient.makeDirectory(dir);
        } catch (Exception e) {
            throw e;
        }
    }

    public void disconnect() {
        try {
            mFtpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean connect(String ip, int port, String userName, String pass) {
        boolean status = false;
        try {
            mFtpClient.connect(ip, port);
            status = mFtpClient.login(userName, pass);
            log("connect result: " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public FTPFile[] listFiles() throws IOException {
        return mFtpClient.listFiles();
    }

    //Passing Local File path/Uri
    public void uploadFile(String uri, String name) throws Exception {
        try {
            File file = new File(uri);
            mFtpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            FileInputStream srcFileStream = new FileInputStream(file);
            boolean status = mFtpClient.storeFile(name, srcFileStream);
            Log.e("Status", String.valueOf(status));
            srcFileStream.close();
        } catch (Exception e) {
            throw e;
        }
    }

    //Passing InputStream and fileName
    public void uploadFile(InputStream srcFileStream, String name) throws Exception {
        try {
            mFtpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            boolean status = mFtpClient.storeFile(name, srcFileStream);
            Log.e("Status", String.valueOf(status));
            srcFileStream.close();
        } catch (Exception e) {
            throw e;
        }
    }

    public void downloadFile(String remoteFilePath, String dest) throws Exception {
        File downloadFile = new File(dest);
        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists())
            parentDir.mkdir();
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            mFtpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            boolean status = mFtpClient.retrieveFile(remoteFilePath, outputStream);
            Log.e("Status", String.valueOf(status));
        } catch (Exception e) {
            throw e;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
    }

    private void log(String msg) {
        Log.i(TAG, msg);
    }
}
