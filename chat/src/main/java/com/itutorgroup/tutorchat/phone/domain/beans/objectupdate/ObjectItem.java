package com.itutorgroup.tutorchat.phone.domain.beans.objectupdate;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/17.
 */
public class ObjectItem {
    @Tag(1)
    public String Field;

    @Tag(2)
    public String Val;

    @Tag(3)
    public int Operate;

    public ObjectItem() {

    }

    @Override
    public String toString() {
        return "ObjectItem{" +
                "Field='" + Field + '\'' +
                ", Val='" + Val + '\'' +
                ", Operate=" + Operate +
                '}';
    }
}
