package com.itutorgroup.tutorchat.phone.domain.beans;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/12/19.
 */
public class SetGroupAdminInfo {
    @Tag(1)
    public String UserID;

    @Tag(2)
    public boolean AddOrRemove;

    @Override
    public String toString() {
        return "SetGroupAdminInfo{" +
                "UserID='" + UserID + '\'' +
                ", AddOrRemove=" + AddOrRemove +
                '}';
    }
}
