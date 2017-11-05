package org.tuzhao.ftp.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.net.ftp.FTPFile;

/**
 * author: tuzhao
 * 2017-11-05 21:22
 */
public final class FileInfo implements Parcelable {

    private String fileName;
    private String path;
    private String size;
    private String lastModified;
    private String isHiddenFile;
    private String permission;
    private String owner;
    private String group;
    private String mimeType;
    private int icon;

    public FileInfo() {
    }

    public void setFileInfo(FTPFile file, String path, int icon) {
        this.fileName = file.getName();
        this.path = path + "/" + this.fileName;
        this.size = String.valueOf(file.getSize());
        this.lastModified = file.getTimestamp().getTime().toString();
        this.isHiddenFile = "no";
        this.permission = "---";
        this.owner = file.getUser();
        this.group = file.getGroup();
        this.mimeType = "unknown";
        this.icon = icon;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }

    public String getSize() {
        return size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String isHiddenFile() {
        return isHiddenFile;
    }

    public String getPermission() {
        return permission;
    }

    public String getOwner() {
        return owner;
    }

    public String getGroup() {
        return group;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getIcon() {
        return icon;
    }

    public FileInfo(Parcel in) {
        fileName = in.readString();
        path = in.readString();
        size = in.readString();
        lastModified = in.readString();
        isHiddenFile = in.readString();
        permission = in.readString();
        owner = in.readString();
        group = in.readString();
        mimeType = in.readString();
        icon = in.readInt();
    }

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeString(path);
        dest.writeString(size);
        dest.writeString(lastModified);
        dest.writeString(isHiddenFile);
        dest.writeString(permission);
        dest.writeString(owner);
        dest.writeString(group);
        dest.writeString(mimeType);
        dest.writeInt(icon);
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                   "fileName='" + fileName + '\'' +
                   ", path='" + path + '\'' +
                   ", size='" + size + '\'' +
                   ", lastModified='" + lastModified + '\'' +
                   ", isHiddenFile='" + isHiddenFile + '\'' +
                   ", permission='" + permission + '\'' +
                   ", owner='" + owner + '\'' +
                   ", group='" + group + '\'' +
                   ", mimeType='" + mimeType + '\'' +
                   ", icon=" + icon +
                   '}';
    }

}
