package org.tuzhao.ftp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.tuzhao.ftp.entity.ServerEntity;
import org.tuzhao.ftp.db.ServerTable.ServerEntry;

import java.util.ArrayList;

/**
 * author: tuzhao
 * 2017-08-11 23:30
 */
public final class RsDBHelper {

    private final RsSQLiteHelper helper;

    public RsDBHelper(Context context) {
        helper = new RsSQLiteHelper(context);
    }

    /**
     * 添加一个server实例信息
     * @param entity ServerEntity
     * @return row ID insert, -1 error occurred
     */
    public long addServer(ServerEntity entity) {
        long newRowId = -1;
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(ServerEntry.COLUMN_NAME_ADDRESS, entity.getAddress());
            values.put(ServerEntry.COLUMN_NAME_PORT, entity.getPort());
            values.put(ServerEntry.COLUMN_NAME_ACCOUNT, entity.getAccount());
            values.put(ServerEntry.COLUMN_NAME_PWD, entity.getPwd());
            newRowId = db.insert(ServerEntry.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != db) {
                    db.endTransaction();
                    db.close();
                }
            } catch (Exception e) {
                //...ignore...
            }
        }
        return newRowId;
    }

    /**
     * 获取存在数据库中所有server实例信息
     * @return ArrayList<ServerEntity>
     */
    public ArrayList<ServerEntity> getServerList() {
        ArrayList<ServerEntity> list = new ArrayList<>();
        String[] args = new String[]{};
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(RsSQLInfo.SQL_QUERY_ENTRIES, args);
        if (cursor.moveToFirst()) {
            int count = cursor.getCount();
            if (count > 0) {
                do {
                    String address = cursor.getString(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_ADDRESS));
                    String port = String.valueOf(cursor.getInt(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_PORT)));
                    String account = cursor.getString(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_ACCOUNT));
                    String password = cursor.getString(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_PWD));
                    ServerEntity entity = new ServerEntity();
                    entity.setAddress(address);
                    entity.setPort(port);
                    entity.setAccount(account);
                    entity.setPwd(password);
                    list.add(entity);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return list;
    }
}
