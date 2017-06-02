package com.itutorgroup.tutorchat.phone.domain.request.v2;

import com.itutorgroup.tutorchat.phone.domain.request.CommonRequest;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/15.
 */
public class SetNewsNoticeDisturbRequest extends CommonRequest {
    @Tag(4)
    public int flag;
}
