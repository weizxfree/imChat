package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/29.
 */
public class VerifyMessageRequest extends CommonRequest{

    @Tag(4)
    public String receiptID ;

    public VerifyMessageRequest() {
    }
}
