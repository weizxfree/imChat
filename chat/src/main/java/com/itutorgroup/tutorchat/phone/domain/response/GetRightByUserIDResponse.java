package com.itutorgroup.tutorchat.phone.domain.response;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/10.
 */
public class GetRightByUserIDResponse extends CommonResponse {
    @Tag(3)
    public List<String> RightCodeList;
}
