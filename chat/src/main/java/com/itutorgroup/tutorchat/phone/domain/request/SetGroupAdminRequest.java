package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/13.
 */
public class SetGroupAdminRequest extends CommonRequest {
    @Tag(4)
    public String GroupID;

    @Tag(5)
    public String ChangeUserID;

    @Tag(6)
    public boolean AddOrRemove;
}
