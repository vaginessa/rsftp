package org.tuzhao.ftp.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.ServerEntity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnectService extends BaseService {

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    private ServerEntity server;
    private RunnableListFiles runnableListFiles;

    public ServerConnectService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServerConnectBinder();
    }

    public class ServerConnectBinder extends Binder {

        public void connect() {
            getService().connect();
        }

        /**
         * ser connect FTP server info
         * @param server FTP server instance
         */
        public void setServer(ServerEntity server) {
            getService().setServer(server);
        }

        /**
         * list of all files of specify FTP server directory path
         * @param path FTP server directory path
         */
        public void listFiles(String path) {
            getService().listFiles(path);
        }

        /**
         * clear current server info<br>
         * cancel all runnable
         */
        public void clear() {
            getService().clearServer();
            getService().cancelListTask();
        }

        /**
         * down the files form ftp server
         * @param list ArrayList
         */
        public void download(ArrayList<RsFile> list, String path) {
            getService().downloadFile(list, path);
        }

        public void delete(ArrayList<RsFile> list, String path) {
            getService().delete(list, path);
        }
    }

    private ServerConnectService getService() {
        return this;
    }

    public void delete(ArrayList<RsFile> list, String path) {
        RunnableDeleteFiles runnable = new RunnableDeleteFiles(getService(), server, list, path);
        executor.execute(runnable);
    }

    public void downloadFile(ArrayList<RsFile> list, String path) {
        RunnableDownloadFiles runnableDownloadFiles = new RunnableDownloadFiles(getService(), server, list, path);
        executor.execute(runnableDownloadFiles);
    }

    private void clearServer() {
        this.server = null;
    }

    private void setServer(ServerEntity server) {
        this.server = server;
    }

    private void cancelListTask() {
        log("clear");
        if (null != runnableListFiles)
            runnableListFiles.taskClose();
        runnableListFiles = null;
    }

    private void listFiles(String path) {
        if (null != runnableListFiles) {
            runnableListFiles.taskClose();
        }
        runnableListFiles = new RunnableListFiles(getService(), server, path);
        executor.execute(runnableListFiles);
    }

    private void connect() {
        RunnableConnect runnable = new RunnableConnect(getService(), server);
        executor.execute(runnable);
    }

}
