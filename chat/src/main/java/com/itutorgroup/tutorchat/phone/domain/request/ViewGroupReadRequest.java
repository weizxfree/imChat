package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/8.
 */
public class ViewGroupReadRequest extends CommonRequest {

    @Tag(4)
    public String MessageID;

    @Tag(5)
    public String GroupID;

}
