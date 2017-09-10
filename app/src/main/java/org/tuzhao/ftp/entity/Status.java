package org.tuzhao.ftp.entity;

/**
 * author: tuzhao
 * 2017-09-10 22:27
 */
public final class Status {
    /**
     * 尚未开始下载  /   尚未开始上传
     */
    public static final int UNDO = 0;
    /**
     * 下载中  /  上传中
     */
    public static final int DOING = 1;
    /**
     * 下载成功  /  上传成功
     */
    public static final int SUCC = 2;
    /**
     * 下载失败  /  上传失败
     */
    public static final int FAIL = 3;

}
