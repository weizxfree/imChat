package com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.patch;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/27.
 */
public class GroupPatches {
    @Tag(1)
    public long TimeSpan;

    @Tag(2)
    public String GroupId;
}
