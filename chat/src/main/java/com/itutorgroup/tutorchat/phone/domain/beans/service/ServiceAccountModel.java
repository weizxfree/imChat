package com.itutorgroup.tutorchat.phone.domain.beans.service;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2017/1/5.
 */
public class ServiceAccountModel implements Serializable {
    @Tag(1)
    public String ServiceAccountId;

    @Tag(2)
    public String Name;

    @Tag(3)
    public String Code;

    @Tag(4)
    public int Type;

    @Tag(5)
    public int Status;

    @Tag(6)
    public String ImageUrl;

    @Tag(7)
    public String Description;

    @Override
    public String toString() {
        return "ServiceAccountModel{" +
                "ServiceAccountId='" + ServiceAccountId + '\'' +
                ", Name='" + Name + '\'' +
                ", Code='" + Code + '\'' +
                ", Type=" + Type +
                ", Status=" + Status +
                ", ImageUrl='" + ImageUrl + '\'' +
                ", Description='" + Description + '\'' +
                '}';
    }
}
