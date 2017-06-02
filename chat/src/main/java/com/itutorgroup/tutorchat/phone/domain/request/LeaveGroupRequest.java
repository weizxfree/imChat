package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/9/6.
 */
public class LeaveGroupRequest extends CommonRequest{

    @Tag(4)
    public String GroupID ;

}
