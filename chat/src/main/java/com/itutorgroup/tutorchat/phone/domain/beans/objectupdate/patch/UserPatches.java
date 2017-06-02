package com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.patch;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/27.
 */
public class UserPatches {
    @Tag(1)
    public String UserId;

    @Tag(2)
    public long TimeSpan;
}
