package com.itutorgroup.tutorchat.phone.domain.request.tcp;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/7.
 */
public class VerifyMessageRequest {
    @Tag(1)
    public long ReceiptID;
}
