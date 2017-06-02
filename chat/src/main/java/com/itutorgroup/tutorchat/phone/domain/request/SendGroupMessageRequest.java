package com.itutorgroup.tutorchat.phone.domain.request;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/24.
 */
public class SendGroupMessageRequest extends CommonRequest {

    @Tag(4)
    public String Content ;
    @Tag(5)
    public int Type ;
    @Tag(6)
    public int Priority  ;
    @Tag(7)
    public String GroupId ;
    @Tag(8)
    public String LocalID ;
    @Tag(9)
    public ArrayList<String> AltReceivers;

    public SendGroupMessageRequest() {
    }
}
