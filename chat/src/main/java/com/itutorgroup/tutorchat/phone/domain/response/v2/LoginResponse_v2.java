package com.itutorgroup.tutorchat.phone.domain.response.v2;

import com.itutorgroup.tutorchat.phone.domain.response.LoginResponse;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/11.
 */
public class LoginResponse_v2 extends LoginResponse {

    @Tag(5)
    public int AESKey;

    @Tag(6)
    public String AESValue;

    @Override
    public String toString() {
        return "LoginResponse_v2{" +
                "AESKey=" + AESKey +
                ", AESValue='" + AESValue + '\'' +
                '}';
    }
}
