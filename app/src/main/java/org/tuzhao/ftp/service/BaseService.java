package org.tuzhao.ftp.service;

import android.app.Service;
import android.util.Log;

/**
 * author: tuzhao
 * 2017-09-10 13:31
 */
public abstract class BaseService extends Service {

    public void log(String msg) {
        Log.d(this.getClass().getSimpleName(), msg);
    }

}
