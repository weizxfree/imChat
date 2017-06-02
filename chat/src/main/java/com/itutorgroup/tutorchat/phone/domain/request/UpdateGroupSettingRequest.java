package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/9/14.
 */
public class UpdateGroupSettingRequest extends CommonRequest{

    @Tag(4)
    public String GroupID ;
    @Tag(5)
    public boolean IsEnable;
    @Tag(6)
    public int Type;
}
