package com.itutorgroup.tutorchat.phone.domain.response;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/18.
 */
public class CommonResponse implements Serializable{

    @Tag(1)
    public int ResultCode;
    @Tag(2)
    public String ResultMsg;

}
