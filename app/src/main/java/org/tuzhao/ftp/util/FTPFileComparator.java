package org.tuzhao.ftp.util;

import org.apache.commons.net.ftp.FTPFile;

import java.util.Comparator;

/**
 * author: tuzhao
 * 2017-08-15 23:05
 */
public class FTPFileComparator implements Comparator<FTPFile> {

    @Override
    public int compare(FTPFile file1, FTPFile file2) {
        boolean b1 = file1.isDirectory();
        boolean b2 = file2.isDirectory();
        if (b1) {
            if (b2) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (b2) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
