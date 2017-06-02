package com.itutorgroup.tutorchat.phone.domain.request;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/17.
 */
public class SetReadRequest extends  CommonRequest{


    @Tag(4)
    public List<String> receiverMessageIDs ;

    @Tag(5)
    public int IsRecipet;



}
