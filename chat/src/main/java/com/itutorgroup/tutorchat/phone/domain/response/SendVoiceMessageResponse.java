package com.itutorgroup.tutorchat.phone.domain.response;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/9/12.
 */
public class SendVoiceMessageResponse extends CommonResponse{

    @Tag(3)
    public String MessageID ;
    @Tag(4)
    public String LocalID ;
}
