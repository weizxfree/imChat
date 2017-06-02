package com.itutorgroup.tutorchat.phone.domain.request;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/1.
 */
public class GetUserByIDsRequest extends CommonRequest {

    @Tag(4)
    public List<String> UserIDList ;

}
