package com.itutorgroup.tutorchat.phone.domain.response.tcp;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/14.
 */
public class TCPAuthResponse {
    @Tag(1)
    public String AesKey;

    @Override
    public String toString() {
        return "TCPAuthResponse{" +
                "AesKey='" + AesKey + '\'' +
                '}';
    }
}
