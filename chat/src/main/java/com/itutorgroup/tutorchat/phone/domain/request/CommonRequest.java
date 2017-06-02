package com.itutorgroup.tutorchat.phone.domain.request;

import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class CommonRequest {
    @Tag(1)
    public int MessageDeviceType;
    @Tag(2)
    public String UserID;
    @Tag(3)
    public String Token;

    @Override
    public String toString() {
        return "CommonRequest{" +
                "MessageDeviceType=" + MessageDeviceType +
                ", UserID='" + UserID + '\'' +
                ", Token='" + Token + '\'' +
                '}';
    }

    public void init() {
        MessageDeviceType = Constant.MESSAGE_DEVICE_TYPE;
        UserID = AccountManager.getInstance().getCurrentUserId();
        Token = AccountManager.getInstance().getToken();
    }
}
