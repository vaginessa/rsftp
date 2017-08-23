package org.tuzhao.ftp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.tuzhao.ftp.db.ServerTable.ServerEntry;
import org.tuzhao.ftp.entity.ServerEntity;

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
                    String savePath = cursor.getString(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_SAVE_PATH));
                    String address = cursor.getString(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_ADDRESS));
                    String port = String.valueOf(cursor.getInt(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_PORT)));
                    String account = cursor.getString(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_ACCOUNT));
                    String password = cursor.getString(cursor.getColumnIndex(ServerEntry.COLUMN_NAME_PWD));
                    int id = cursor.getInt(cursor.getColumnIndex(ServerEntry._ID));
                    ServerEntity entity = new ServerEntity();
                    entity.setSavePath(savePath);
                    entity.setAddress(address);
                    entity.setPort(port);
                    entity.setAccount(account);
                    entity.setPwd(password);
                    entity.setId(id);
                    list.add(entity);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return list;
    }

    public int deleteServer(ServerEntity server) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String selection = ServerEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(server.getId())};
        return db.delete(ServerEntry.TABLE_NAME, selection, selectionArgs);
    }

    public int updateServer(ServerEntity server) {
        SQLiteDatabase db = null;
        int update = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(ServerEntry.COLUMN_NAME_SAVE_PATH, server.getSavePath());
            values.put(ServerEntry.COLUMN_NAME_ADDRESS, server.getAddress());
            values.put(ServerEntry.COLUMN_NAME_PORT, server.getPort());
            values.put(ServerEntry.COLUMN_NAME_ACCOUNT, server.getAccount());
            values.put(ServerEntry.COLUMN_NAME_PWD, server.getPwd());
            String selection = ServerEntry._ID + "=?";
            String[] selectionArgs = {String.valueOf(server.getId())};

            db = helper.getWritableDatabase();
            db.beginTransaction();
            update = db.update(ServerEntry.TABLE_NAME, values, selection, selectionArgs);
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
        return update;
    }

}
