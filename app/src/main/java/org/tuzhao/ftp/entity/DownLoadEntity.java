package org.tuzhao.ftp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author: tuzhao
 * 2017-09-09 11:32
 */
public class DownLoadEntity implements Parcelable {

    private String fileName;
    /**
     * 0 -> 尚未开始下载  /   尚未开始上传
     * 1 ->下载中  /  上传中
     * 2 -> 下载成功  /  上传成功
     * 3 -> 下载失败  /  上传失败
     */
    private int status;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DownLoadEntity(Parcel in) {
        fileName = in.readString();
        status = in.readInt();
    }

    public DownLoadEntity() {
    }

    public static final Creator<DownLoadEntity> CREATOR = new Creator<DownLoadEntity>() {
        @Override
        public DownLoadEntity createFromParcel(Parcel in) {
            return new DownLoadEntity(in);
        }

        @Override
        public DownLoadEntity[] newArray(int size) {
            return new DownLoadEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fileName);
        parcel.writeInt(status);
    }

    @Override
    public String toString() {
        return "DownLoadEntity{" +
                   "fileName='" + fileName + '\'' +
                   ", status='" + status + '\'' +
                   '}' + " " + super.toString();
    }
}
