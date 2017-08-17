package org.tuzhao.ftp.util;

import org.apache.commons.net.ftp.FTPFile;
import org.tuzhao.ftp.R;

/**
 * author: tuzhao
 * 2017-08-17 23:34
 */
public final class FileType {

    public static int getFileDesImg(FTPFile file) {
        int img = R.drawable.file_unknown;
        if (file.isDirectory()) {
            img = R.drawable.folder;
        } else if (file.isFile()) {
            String type = getFileType(file.getName());
            if (type.equals(".gz")) {
                if (file.getName().endsWith(".tar.gz")) {
                    img = R.drawable.file_tar_gz;
                    return img;
                }
            }
            if (type.equals(".bz2")) {
                if (file.getName().endsWith(".tar.bz2")) {
                    img = R.drawable.file_tar_bz2;
                    return img;
                }
            }
            switch (type) {
                case ".jpg":
                case ".png":
                    img = R.drawable.file_image;
                    break;
                case ".pdf":
                    img = R.drawable.file_pdf;
                    break;
                case ".txt":
                    img = R.drawable.file_txt;
                    break;
                case ".xml":
                    img = R.drawable.file_xml;
                    break;
                case ".doc":
                    img = R.drawable.file_doc;
                    break;
                case ".ppt":
                    img = R.drawable.file_ppt;
                    break;
                case ".mp3":
                    img = R.drawable.file_audio;
                    break;
                case ".mp4":
                case ".avi":
                case ".rmvb":
                    img = R.drawable.file_vedio;
                    break;
                case ".apk":
                    img = R.drawable.file_apk;
                    break;
                case ".sql":
                    img = R.drawable.file_sql;
                    break;
                case ".tar":
                    img = R.drawable.file_tar;
                    break;
                case ".gz":
                    img = R.drawable.file_gz;
                    break;
                case ".jar":
                    img = R.drawable.file_jar;
                    break;
                case ".7z":
                    img = R.drawable.file_7z;
                    break;
                case ".zip":
                case ".rar":
                    img = R.drawable.file_zip;
                    break;
                case ".bz2":
                    img = R.drawable.file_bz2;
                    break;
                default:
                    img = R.drawable.file_file;
                    break;
            }
        }
        return img;
    }


    private static String getFileType(String name) {
        String type = "unknown";
        try {
            int lastIndexOf = name.lastIndexOf(".");
            type = name.substring(lastIndexOf, name.length());
        } catch (Exception e) {
            //...ignore...
        }
        return type;
    }

}