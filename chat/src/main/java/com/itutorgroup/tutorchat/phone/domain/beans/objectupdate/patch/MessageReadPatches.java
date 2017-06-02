package com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.patch;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/21.
 */
public class MessageReadPatches {
    @Tag(1)
    public ArrayList<Long> MessageIdList;
}
