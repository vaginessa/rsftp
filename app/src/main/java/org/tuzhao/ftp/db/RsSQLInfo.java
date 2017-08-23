package org.tuzhao.ftp.db;

import org.tuzhao.ftp.db.ServerTable.ServerEntry;

/**
 * author: tuzhao
 * 2017-08-11 23:25
 */
final class RsSQLInfo {

    private static final String TEXT_TYPE = " VARCHAR(30)";
    private static final String NUM_TYPE = " INTEGER";
    private static final String NOT_NULL = " NOT NULL";

    private static final String COMMA_SEP = ",";

    static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE IF NOT EXISTS " + ServerEntry.TABLE_NAME + " (" +
            ServerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ServerEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            ServerEntry.COLUMN_NAME_PORT + NUM_TYPE + " DEFAULT 2121" + COMMA_SEP +
            ServerEntry.COLUMN_NAME_ACCOUNT + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            ServerEntry.COLUMN_NAME_PWD + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            ServerEntry.COLUMN_NAME_SAVE_PATH + " VARCHAR(500)" +
            " )";

    public static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + ServerEntry.TABLE_NAME;

    public static final String SQL_QUERY_ENTRIES = "select * from " + ServerEntry.TABLE_NAME + " order by " + ServerEntry._ID;
}
