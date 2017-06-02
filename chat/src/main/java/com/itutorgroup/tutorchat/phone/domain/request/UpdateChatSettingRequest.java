package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class UpdateChatSettingRequest extends CommonRequest {


    @Tag(4)
    public String ContractUserID ;
    @Tag(5)
    public boolean IsEnable;
    @Tag(6)
    public int Type;
}
