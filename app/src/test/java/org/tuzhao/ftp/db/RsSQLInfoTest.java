package org.tuzhao.ftp.db;

import org.junit.Test;

/**
 * author: tuzhao
 * 2017-08-12 00:04
 */
public class RsSQLInfoTest {

    @Test
    public void printSQLInfo() {
        println(RsSQLInfo.SQL_CREATE_ENTRIES);
        println(RsSQLInfo.SQL_DELETE_ENTRIES);
        println(RsSQLInfo.SQL_QUERY_ENTRIES);
    }

    private void println(Object msg) {
        System.out.println(String.valueOf(msg));
    }

}