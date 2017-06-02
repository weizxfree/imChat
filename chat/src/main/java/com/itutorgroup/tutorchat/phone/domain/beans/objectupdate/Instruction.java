package com.itutorgroup.tutorchat.phone.domain.beans.objectupdate;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/18.
 */
public class Instruction {
    @Tag(1)
    public int InstType;

    @Tag(2)
    public byte[] InstData;

    @Tag(3)
    public String PostUserID;

    @Override
    public String toString() {
        return "Instruction{" +
                "InstType=" + InstType +
                ", PostUserID='" + PostUserID + '\'' +
                '}';
    }
}
