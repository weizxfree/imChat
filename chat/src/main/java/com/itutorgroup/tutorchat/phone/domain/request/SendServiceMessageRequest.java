package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2017/1/5.
 */
public class SendServiceMessageRequest  extends CommonRequest {
    @Tag(4)
    public String ServiceAccountId;
    @Tag(5)
    public String content;
    @Tag(6)
    public int Type ;

}
