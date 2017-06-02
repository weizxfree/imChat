package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/16.
 */
public class ConnectRequest extends CommonRequest{
    @Tag(4)
    public String DeviceID ;
    @Tag(5)
    public int DeviceType ;
    @Tag(6)
    public int LanguageType;

    public ConnectRequest(){


    }

    public ConnectRequest(int messageDeviceType, String userID, String token, String deviceID, int deviceType) {
        MessageDeviceType = messageDeviceType;
        UserID = userID;
        Token = token;
        DeviceID = deviceID;
        DeviceType = deviceType;
    }



}
