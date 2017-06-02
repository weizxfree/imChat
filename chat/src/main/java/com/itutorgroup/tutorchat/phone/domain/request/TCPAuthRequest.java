package com.itutorgroup.tutorchat.phone.domain.request;

import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/10/31.
 */
public class TCPAuthRequest {
    @Tag(1)
    public String Token;
    @Tag(2)
    public String UserID;
    @Tag(3)
    public int DeviceType;

    @Override
    public String toString() {
        return "TCPAuthRequest{" +
                "Token='" + Token + '\'' +
                ", UserID='" + UserID + '\'' +
                ", DeviceType=" + DeviceType +
                '}';
    }

    public void init() {
        DeviceType = Constant.MESSAGE_DEVICE_TYPE;
        UserID = AccountManager.getInstance().getCurrentUserId();
        Token = AccountManager.getInstance().getToken();
    }
}
