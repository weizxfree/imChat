package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class UpdateUserSettingRequest extends CommonRequest {
    @Tag(4)
    public boolean IsEnable;
    @Tag(5)
    public int Type;
}
