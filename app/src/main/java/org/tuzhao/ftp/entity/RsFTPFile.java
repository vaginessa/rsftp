package org.tuzhao.ftp.entity;

import org.apache.commons.net.ftp.FTPFile;

/**
 * author: tuzhao
 * 2017-08-21 20:39
 */
public final class RsFTPFile implements RsFile {

    private FTPFile file;

    public RsFTPFile(FTPFile file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long getModifyTimeMillis() {
        return file.getTimestamp().getTimeInMillis();
    }

    @Override
    public long getSize() {
        return file.getSize();
    }

    @Override
    public boolean isDir() {
        return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

}
