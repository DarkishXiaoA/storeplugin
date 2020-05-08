package com.github.DarkishXiaoA.entity;

public class GoodEntity {
    private String orderid;
    private String cmd;
    private String type;

    @Override
    public String toString() {
        return "GoodEntity{" +
                "orderid='" + orderid + '\'' +
                ", cmd='" + cmd + '\'' +
                '}';
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public GoodEntity(String orderid, String cmd) {
        this.orderid = orderid;
        this.cmd = cmd;
    }
}
