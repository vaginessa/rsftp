package org.tuzhao.ftp.util;

import org.tuzhao.ftp.entity.RsFile;

import java.util.Comparator;

/**
 * author: tuzhao
 * 2017-08-15 23:05
 */
public class RsFileComparator implements Comparator<RsFile> {

    @Override
    public int compare(RsFile file1, RsFile file2) {
        boolean b1 = file1.isDir();
        boolean b2 = file2.isDir();
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
