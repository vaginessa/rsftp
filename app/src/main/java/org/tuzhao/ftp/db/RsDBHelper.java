package org.tuzhao.ftp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.db.ServerTable.ServerEntry;

/**
 * author: tuzhao
 * 2017-08-11 23:30
 */
public final class RsDBHelper {

    private final RsSQLiteHelper helper;

    public RsDBHelper(Context context) {
        helper = new RsSQLiteHelper(context);
    }

    public long addServer(ServerEntity entity) {
        long newRowId = -1;
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(ServerEntry.COLUMN_NAME_ADDRESS, entity.getAddress());
            values.put(ServerEntry.COLUMN_NAME_PORT, entity.getPort());
            values.put(ServerEntry.COLUMN_NAME_ACCOUNT, entity.getAccount());
            values.put(ServerEntry.COLUMN_NAME_PWD, entity.getPwd());
            newRowId = db.insert(ServerEntry.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newRowId;
    }
}
