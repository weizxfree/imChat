package com.itutorgroup.tutorchat.phone.domain.request;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/17.
 */
public class CreateGroupRequest extends CommonRequest {

    @Tag(4)
    public String GroupName;
    @Tag(5)
    public List<String> UserIDList;
    @Tag(6)
    public String Description;

    public CreateGroupRequest() {
    }
}
