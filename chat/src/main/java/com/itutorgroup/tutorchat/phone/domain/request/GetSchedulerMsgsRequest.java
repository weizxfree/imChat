package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/16.
 */
public class GetSchedulerMsgsRequest extends CommonRequest {

    @Tag(4)
    public String receiverID ;
}
