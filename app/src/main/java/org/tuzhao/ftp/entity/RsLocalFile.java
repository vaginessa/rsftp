package org.tuzhao.ftp.entity;

import java.io.File;

/**
 * author: tuzhao
 * 2017-08-21 20:39
 */
public class RsLocalFile implements RsFile<File> {

    private File file;
    private boolean select;

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

    @Override
    public boolean canExecute() {
        return this.file.canExecute();
    }

    @Override
    public boolean canRead() {
        return this.file.canRead();
    }

    @Override
    public boolean canWrite() {
        return this.file.canWrite();
    }

    @Override
    public void setSelected(boolean flag) {
        this.select = flag;
    }

    @Override
    public boolean getSelected() {
        return this.select;
    }

    @Override
    public File getRealFile() {
        return this.file;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }
}
