package org.tuzhao.ftp.entity;

/**
 * author: tuzhao
 * 2017-08-21 20:38
 */
public interface RsFile {

    String getName();

    long getModifyTimeMillis();

    long getSize();

    boolean isDir();

    boolean isFile();


}
