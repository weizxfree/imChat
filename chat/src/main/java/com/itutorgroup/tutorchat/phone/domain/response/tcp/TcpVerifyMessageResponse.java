package com.itutorgroup.tutorchat.phone.domain.response.tcp;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/8.
 */
public class TcpVerifyMessageResponse {
    @Tag(1)
    public long ReceiptID;

    @Override
    public String toString() {
        return "TcpVerifyMessageResponse{" +
                "ReceiptID=" + ReceiptID +
                '}';
    }
}
