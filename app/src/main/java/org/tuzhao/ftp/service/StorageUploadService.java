package org.tuzhao.ftp.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.tuzhao.ftp.entity.RsFile;
import org.tuzhao.ftp.entity.ServerEntity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageUploadService extends BaseService {

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public StorageUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new StorageUploadBinder();
    }

    public class StorageUploadBinder extends Binder {
        public void upload(ServerEntity server, ArrayList<RsFile> selectedList, String serverPath) {
            getService().upload(server, selectedList, serverPath);
        }
    }

    private StorageUploadService getService() {
        return this;
    }

    private void upload(ServerEntity server, ArrayList<RsFile> selectedList, String serverPath) {
        executor.execute(new RunnableUploadFiles(getService(), server, selectedList, serverPath));
    }

}
