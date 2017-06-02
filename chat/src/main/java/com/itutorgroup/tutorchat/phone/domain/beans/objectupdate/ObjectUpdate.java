package com.itutorgroup.tutorchat.phone.domain.beans.objectupdate;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/17.
 */
public class ObjectUpdate {
    @Tag(1)
    public int tableName;
    @Tag(2)
    public String tableObjId;
    @Tag(3)
    public long updateTime;
    @Tag(4)
    public ArrayList<ObjectItem> items;

    public ObjectUpdate() {

    }

    @Override
    public String toString() {
        return "ObjectUpdate{" +
                "tableName=" + tableName +
                ", tableObjId='" + tableObjId + '\'' +
                ", updateTime=" + updateTime +
                ", items=" + items +
                '}';
    }
}
