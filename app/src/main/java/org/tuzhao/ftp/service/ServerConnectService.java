package org.tuzhao.ftp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.tuzhao.ftp.entity.ServerEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnectService extends Service {

    private static final String TAG = "ServerConnectService";

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    private ServerEntity server;
    private RunnableListFiles runnableListFiles;

    public ServerConnectService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServerConnectBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
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
    }

    private ServerConnectService getService() {
        return this;
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

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

}
