package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/7.
 */
public class CheckFileIsExitRequest extends CommonRequest {

    @Tag(4)
    public String FileMD5 ;

    @Tag(5)
    public int ByteLength;


}
