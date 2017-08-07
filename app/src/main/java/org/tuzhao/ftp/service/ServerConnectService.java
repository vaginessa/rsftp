package org.tuzhao.ftp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import org.tuzhao.ftp.activity.ServerActivity;
import org.tuzhao.ftp.entity.ServerAddItem;
import org.tuzhao.ftp.util.ServerClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerConnectService extends Service {

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    private static final int MSG_CONNECT = 0x10;

    private Handler handler;
    private ServerClient client;

    public ServerConnectService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(message -> {
            switch (message.what) {
                case MSG_CONNECT:
                    boolean result = (boolean) message.obj;
                    if (!result) client = null;
                    ServerActivity.sendConnectResult(getService(), result);
                    break;
            }
            return true;
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServerConnectBinder();
    }

    public class ServerConnectBinder extends Binder {
        public void connect(ServerAddItem server) {
            getService().connect(server);
        }
    }

    private ServerConnectService getService() {
        return this;
    }

    private void connect(ServerAddItem server) {
        RunnableConnect runnable = new RunnableConnect(handler, server);
        client = runnable.getServerClient();
        executor.execute(runnable);
    }

    private static class RunnableConnect implements Runnable {

        private Handler handler;
        private ServerAddItem server;
        private ServerClient client;

        RunnableConnect(Handler handler, ServerAddItem server) {
            this.handler = handler;
            this.server = server;
            this.client = new ServerClient();
        }

        ServerClient getServerClient() {
            return client;
        }

        @Override
        public void run() {
            String address = server.getAddress();
            int port = Integer.parseInt(server.getPort());
            String account = server.getAccount();
            String pwd = server.getPwd();
            boolean result = client.connect(address, port, account, pwd);
            Message message = Message.obtain(handler, MSG_CONNECT, result);
            message.sendToTarget();
        }
    }

}
