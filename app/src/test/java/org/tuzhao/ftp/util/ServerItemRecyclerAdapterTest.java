package org.tuzhao.ftp.util;

import junit.framework.TestCase;

import java.lang.*;
import java.lang.System;

/**
 * author: tuzhao
 * 2017-08-15 22:16
 */
public class ServerItemRecyclerAdapterTest extends TestCase {

    public void testGetDate() throws Exception {
        long time = 1491058200000L;
        String date = ServerItemRecyclerAdapter.getDate(time);
        System.out.println("date: " + date);
    }

}