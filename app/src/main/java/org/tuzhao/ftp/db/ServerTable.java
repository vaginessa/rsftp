package org.tuzhao.ftp.db;

import android.provider.BaseColumns;

/**
 * author: tuzhao
 * 2017-08-11 23:06
 */
public final class ServerTable {

    private ServerTable() {
    }

    public static class ServerEntry implements BaseColumns {
        public static final String TABLE_NAME = "server_list";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_PORT = "port";
        public static final String COLUMN_NAME_ACCOUNT = "account";
        public static final String COLUMN_NAME_PWD = "password";
        public static final String COLUMN_NAME_SAVE_PATH = "save_path";
        public static final String COLUMN_NAME_ENCODING = "encoding";
    }

}
