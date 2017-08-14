package org.tuzhao.ftp.service;

import android.content.Context;

import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.util.WeakRunnable;

/**
 * author: tuzhao
 * 2017-08-14 23:58
 */
class RunnableConnect extends WeakRunnable<Context> {

    private Context context;
    private ServerEntity server;


    RunnableConnect(Context context, ServerEntity server) {
        super(context);
        this.context = context;
        this.server = server;
    }

    @Override
    public void weakRun(Context context) {

    }

}