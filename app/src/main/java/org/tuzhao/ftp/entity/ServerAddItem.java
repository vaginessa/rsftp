package org.tuzhao.ftp.entity;

import java.io.Serializable;

/**
 * zhaotu
 * 17-8-7
 */
public final class ServerAddItem implements Serializable {

    private String address;
    private String port;
    private String account;
    private String pwd;

    public ServerAddItem() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
