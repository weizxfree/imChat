package com.itutorgroup.tutorchat.phone.domain.beans.objectupdate;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/17.
 */
public class ObjectIdTime {
    @Tag(1)
    public String itemId;

    @Tag(2)
    public long ticks;

    public ObjectIdTime() {

    }

    public ObjectIdTime(String id) {
        itemId = id;
    }

    @Override
    public String toString() {
        return "ObjectIdTime{" +
                "itemId='" + itemId + '\'' +
                ", ticks=" + ticks +
                '}';
    }
}
