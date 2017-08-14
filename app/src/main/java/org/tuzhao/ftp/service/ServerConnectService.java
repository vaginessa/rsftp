package org.tuzhao.ftp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import org.tuzhao.ftp.entity.ServerEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnectService extends Service {

    public static final String EXTRA_START = "EXTRA_START";

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    private ServerEntity server;

    public ServerConnectService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Parcelable parcelable = intent.getParcelableExtra(EXTRA_START);
        if (null != parcelable && (parcelable instanceof ServerEntity)) {
            server = (ServerEntity) parcelable;
            log("server info: " + server);
        }
        return new ServerConnectBinder();
    }

    public class ServerConnectBinder extends Binder {

        public void connect() {
            getService().connect();
        }

        public void listFiles() {
            getService().listFiles();
        }
    }

    private ServerConnectService getService() {
        return this;
    }

    private void listFiles() {
        executor.execute(new RunnableListFiles(getService(), server));
    }

    private void connect() {
        RunnableConnect runnable = new RunnableConnect(getService(), server);
        executor.execute(runnable);
    }

    private static void log(String msg) {
        Log.d("ServerConnectService", msg);
    }

}
