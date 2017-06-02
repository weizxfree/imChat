package com.itutorgroup.tutorchat.phone.domain.response;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/29.
 */
public class SendMessageResponse extends CommonResponse{


    @Tag(3)
    public String MessageID ;
    @Tag(4)
    public String LocalID ;
    @Tag(5)
    public int isReceipt;
    @Tag(6)
    public long InsertTime;

    public SendMessageResponse() {
    }

    @Override
    public String toString() {
        return "SendMessageResponse{" +
                "MessageID='" + MessageID + '\'' +
                ", LocalID='" + LocalID + '\'' +
                '}';
    }
}
