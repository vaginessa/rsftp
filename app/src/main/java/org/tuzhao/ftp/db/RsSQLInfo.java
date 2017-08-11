package org.tuzhao.ftp.db;

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
        "CREATE TABLE IF NOT EXISTS " + ServerTable.ServerEntry.TABLE_NAME + " (" +
            ServerTable.ServerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ServerTable.ServerEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            ServerTable.ServerEntry.COLUMN_NAME_PORT + NUM_TYPE + " DEFAULT 2121" + COMMA_SEP +
            ServerTable.ServerEntry.COLUMN_NAME_ACCOUNT + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            ServerTable.ServerEntry.COLUMN_NAME_PWD + TEXT_TYPE + NOT_NULL +
            " )";

    public static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + ServerTable.ServerEntry.TABLE_NAME;
}
