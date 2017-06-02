package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/9/12.
 */
public class SendVoiceMessageRequest extends CommonRequest{

    @Tag(4)
    public byte[] Content ;
    @Tag(5)
    public int Priority;
    @Tag(6)
    public String ReceiverID;
    @Tag(7)
    public int ReceiverType ;
    @Tag(8)
    public String LocalID ;

    @Override
    public String toString() {
        return "SendVoiceMessageRequest{" +
                ", Priority=" + Priority +
                ", ReceiverID='" + ReceiverID + '\'' +
                ", ReceiverType=" + ReceiverType +
                ", LocalID='" + LocalID + '\'' +
                '}';
    }
}
