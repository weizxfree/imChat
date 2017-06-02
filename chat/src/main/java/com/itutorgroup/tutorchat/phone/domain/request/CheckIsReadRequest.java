package com.itutorgroup.tutorchat.phone.domain.request;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/9/1.
 */
public class CheckIsReadRequest extends CommonRequest {


    @Tag(4)
    public List<String> MessageIDs ;


}
