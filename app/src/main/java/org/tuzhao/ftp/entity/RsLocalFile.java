package org.tuzhao.ftp.entity;

import java.io.File;

/**
 * author: tuzhao
 * 2017-08-21 20:39
 */
public class RsLocalFile implements RsFile {

    private File file;

    public RsLocalFile(File file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long getModifyTimeMillis() {
        return file.lastModified();
    }

    @Override
    public long getSize() {
        return file.length();
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
