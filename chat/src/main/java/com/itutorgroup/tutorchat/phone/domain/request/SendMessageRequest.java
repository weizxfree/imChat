package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/29.
 */
public class SendMessageRequest extends CommonRequest {

    @Tag(4)
    public String Content ;
    @Tag(5)
    public int Type ;
    @Tag(6)
    public int Priority;
    @Tag(7)
    public String ReceiverID ;
    @Tag(8)
    public String LocalID ;

}
