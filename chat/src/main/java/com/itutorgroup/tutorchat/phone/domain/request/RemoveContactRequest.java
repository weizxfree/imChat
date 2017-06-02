package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/17.
 */
public class RemoveContactRequest extends CommonRequest{
    @Tag(4)
    public String RemoveID;
    @Tag(5)
    public int RemoveType ; //1：人，2：群组
}
