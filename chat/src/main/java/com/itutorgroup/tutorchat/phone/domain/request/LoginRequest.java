package com.itutorgroup.tutorchat.phone.domain.request;

import com.itutorgroup.tutorchat.phone.config.Constant;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/15.
 */
public class LoginRequest implements Serializable{

    @Tag(1)
    public String Email;

    @Tag(2)
    public String Password;

    @Tag(3)
    public int MessageDeviceType;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        Email = email;
        Password = password;
        MessageDeviceType = Constant.MESSAGE_DEVICE_TYPE;
    }




}
